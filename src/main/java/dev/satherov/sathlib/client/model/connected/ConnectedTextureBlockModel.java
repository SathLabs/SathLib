package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

///
/// Dynamic block model that remaps quads to connected texture atlas tiles at render time.
///
@SuppressWarnings("deprecation")
@NothingNull
public final class ConnectedTextureBlockModel implements DynamicBlockStateModel {
    
    ///
    /// Type identifier used by the connected texture blockstate model codec.
    ///
    public static final Identifier ID = SathLib.id("connected_texture");
    
    private final BlockStateModel delegate;
    private final @Nullable Identifier predicate;
    private final List<SLConnectedTextureConnection> connections;
    private final Material.Baked particleMaterial;
    
    @BakedQuad.MaterialFlags
    private final int materialFlags;
    
    ///
    /// Creates a connected texture block model.
    ///
    /// @param delegate    base baked block model
    /// @param predicate   predicate identifier used for the base texture, or `null`
    /// @param connections explicit connected texture bindings keyed by sprite
    ///
    public ConnectedTextureBlockModel(
            final BlockStateModel delegate,
            @Nullable final Identifier predicate,
            final List<SLConnectedTextureConnection> connections
    ) {
        this.delegate = delegate;
        this.predicate = predicate;
        this.connections = List.copyOf(connections);
        this.particleMaterial = this.resolveParticleMaterial(delegate.particleMaterial());
        this.materialFlags = delegate.materialFlags();
    }
    
    @Override
    public void collectParts(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final RandomSource random,
            final List<BlockStateModelPart> parts
    ) {
        final ConnectedTextureFaceMasks faceMasks = this.predicate != null
                ? ConnectedTextureFaceMasks.resolve(level, pos, state, ConnectedTexturePredicateRegistry.resolve(this.predicate))
                : null;
        final Map<Identifier, ConnectedTextureFaceMasks> spriteMasks = this.resolveConnections(level, pos, state);
        
        final List<BlockStateModelPart> delegateParts = new ArrayList<>();
        this.delegate.collectParts(level, pos, state, random, delegateParts);
        
        for (final BlockStateModelPart part : delegateParts) {
            parts.add(new ConnectedTexturePart(part, faceMasks, spriteMasks));
        }
    }
    
    @Override
    public Object createGeometryKey(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final RandomSource random
    ) {
        final Object delegateKey = this.delegate.createGeometryKey(level, pos, state, random);
        return new GeometryKey(
                this,
                delegateKey,
                this.predicate != null ? ConnectedTextureFaceMasks.resolve(level, pos, state, ConnectedTexturePredicateRegistry.resolve(this.predicate)) : null,
                this.resolveConnections(level, pos, state)
        );
    }
    
    @Override
    public Material.Baked particleMaterial() {
        return this.particleMaterial;
    }
    
    @Override
    public Material.Baked particleMaterial(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        return this.resolveParticleMaterial(this.delegate.particleMaterial(level, pos, state));
    }
    
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.materialFlags;
    }
    
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        return this.delegate.materialFlags(level, pos, state);
    }
    
    ///
    /// Resolves the connections
    ///
    private Map<Identifier, ConnectedTextureFaceMasks> resolveConnections(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state
    ) {
        if (this.connections.isEmpty()) return Map.of();
        
        final HashMap<Identifier, ConnectedTextureFaceMasks> resolved = new HashMap<>(this.connections.size());
        
        for (final SLConnectedTextureConnection connection : this.connections) {
            resolved.put(connection.texture().sprite(), ConnectedTextureFaceMasks.resolve(level, pos, state, connection.rule()));
        }
        
        return Map.copyOf(resolved);
    }
    
    private Material.Baked resolveParticleMaterial(final Material.Baked material) {
        return this.shouldRemapParticle(material.sprite())
                ? ConnectedTextureUvResolver.remap(material, SpriteSheet.Pos.ORIGIN)
                : material;
    }
    
    private boolean shouldRemapParticle(final TextureAtlasSprite sprite) {
        if (this.predicate != null && this.connections.isEmpty()) return true;
        
        final Identifier spriteId = sprite.contents().name();
        
        for (final SLConnectedTextureConnection connection : this.connections) {
            if (connection.texture().sprite().equals(spriteId)) {
                return true;
            }
        }
        
        return false;
    }
    
    private record GeometryKey(
            ConnectedTextureBlockModel model,
            @Nullable Object delegateKey,
            @Nullable ConnectedTextureFaceMasks faceMasks,
            Map<Identifier, ConnectedTextureFaceMasks> spriteMasks
    ) { }
    
    ///
    /// Unbaked connected texture model definition used by blockstate codecs.
    ///
    /// @param model       base unbaked blockstate model
    /// @param predicate   predicate identifier used for the base texture, or `null`
    /// @param connections explicit connected texture bindings
    ///
    public record Unbaked(
            BlockStateModel.Unbaked model,
            @Nullable Identifier predicate,
            List<SLConnectedTextureConnection> connections
    ) implements CustomUnbakedBlockStateModel {
        
        ///
        /// Codec used to serialize unbaked connected texture block models.
        ///
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("model").forGetter(Unbaked::model),
                Identifier.CODEC.optionalFieldOf("predicate").forGetter(unbaked -> java.util.Optional.ofNullable(unbaked.predicate())),
                SLConnectedTextureConnection.CODEC.listOf().optionalFieldOf("connections", List.of()).forGetter(Unbaked::connections)
        ).apply(instance, (model, predicate, connections) -> new Unbaked(model, predicate.orElse(null), connections)));
        
        ///
        /// Creates an unbaked connected texture model that only uses a predicate.
        ///
        /// @param model     base unbaked blockstate model
        /// @param predicate predicate identifier used for the base texture
        ///
        public Unbaked(final BlockStateModel.Unbaked model, final Identifier predicate) {
            this(model, predicate, List.of());
        }
        
        ///
        /// Normalizes the connection list and validates that at least one connection strategy is present.
        ///
        /// @param model       base unbaked blockstate model
        /// @param predicate   predicate identifier used for the base texture, or `null`
        /// @param connections explicit connected texture bindings
        ///
        public Unbaked {
            connections = List.copyOf(connections);
            
            if (predicate == null && connections.isEmpty()) {
                throw new IllegalArgumentException("Connected texture model requires either a predicate or at least one connection");
            }
        }
        
        @Override
        public BlockStateModel bake(final ModelBaker baker) {
            return new ConnectedTextureBlockModel(this.model.bake(baker), this.predicate, this.connections);
        }
        
        @Override
        public void resolveDependencies(final ResolvableModel.Resolver resolver) {
            this.model.resolveDependencies(resolver);
        }
        
        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return Unbaked.MAP_CODEC;
        }
    }
    
    @SuppressWarnings("deprecation")
    private static final class ConnectedTexturePart implements BlockStateModelPart {
        
        private final BlockStateModelPart delegate;
        @Nullable
        private final ConnectedTextureFaceMasks faceMasks;
        private final Map<Identifier, ConnectedTextureFaceMasks> spriteMasks;
        private final EnumMap<Direction, List<BakedQuad>> sidedQuads = new EnumMap<>(Direction.class);
        @Nullable
        private List<BakedQuad> unsidedQuads;
        
        private ConnectedTexturePart(
                final BlockStateModelPart delegate,
                @Nullable final ConnectedTextureFaceMasks faceMasks,
                final Map<Identifier, ConnectedTextureFaceMasks> spriteMasks
        ) {
            this.delegate = delegate;
            this.faceMasks = faceMasks;
            this.spriteMasks = spriteMasks;
        }
        
        @Override
        public List<BakedQuad> getQuads(@Nullable final Direction direction) {
            if (direction == null) {
                if (this.unsidedQuads == null) {
                    this.unsidedQuads = this.remap(this.delegate.getQuads(null), null);
                }
                
                return this.unsidedQuads;
            }
            
            return this.sidedQuads.computeIfAbsent(direction, side -> this.remap(this.delegate.getQuads(side), side));
        }
        
        @Override
        public boolean useAmbientOcclusion() {
            return this.delegate.useAmbientOcclusion();
        }
        
        @Override
        public Material.Baked particleMaterial() {
            return this.delegate.particleMaterial();
        }
        
        @Override
        @BakedQuad.MaterialFlags
        public int materialFlags() {
            return this.delegate.materialFlags();
        }
        
        private List<BakedQuad> remap(final List<BakedQuad> quads, @Nullable final Direction direction) {
            if (quads.isEmpty()) {
                return quads;
            }
            
            final List<BakedQuad> remapped = new ArrayList<>(quads.size());
            
            for (final BakedQuad quad : quads) {
                final Direction face = direction != null ? direction : quad.direction();
                final ConnectedTextureFaceMasks connectionMasks = this.faceMasks != null
                        ? this.faceMasks
                        : this.spriteMasks.get(quad.materialInfo().sprite().contents().name());
                
                remapped.add(connectionMasks != null
                        ? ConnectedTextureUvResolver.remap(quad, SpriteSheet.resolve(connectionMasks.mask(face)))
                        : quad);
            }
            
            return List.copyOf(remapped);
        }
    }
}
