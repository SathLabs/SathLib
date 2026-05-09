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
@NothingNull
@SuppressWarnings("deprecation")
public class ConnectedTextureBlockModel implements DynamicBlockStateModel {
    
    ///
    /// The identifier of the connected texture block state model
    ///
    public static final Identifier ID = SathLib.id("connected_texture");
    
    private final BlockStateModel delegate;
    private final List<ConnectedTextureLayer> layers;
    private final Material.Baked particleMaterial;
    private final @BakedQuad.MaterialFlags int materialFlags;
    
    ///
    /// Creates a connected texture block model.
    ///
    /// @param delegate base baked block model
    /// @param layers   explicit connected texture bindings keyed by sprite
    ///
    public ConnectedTextureBlockModel(final BlockStateModel delegate, final List<ConnectedTextureLayer> layers) {
        this.delegate = delegate;
        this.layers = List.copyOf(layers);
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
        final ConnectionMasks spriteMasks = this.resolveConnections(level, pos, state);
        final List<BlockStateModelPart> delegateParts = new ArrayList<>();
        this.delegate.collectParts(level, pos, state, random, delegateParts);
        
        for (final BlockStateModelPart part : delegateParts) {
            parts.add(new ConnectedTextureBlockModel.ConnectedTexturePart(part, spriteMasks));
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
        return new GeometryKey(delegateKey, this.resolveConnections(level, pos, state));
    }
    
    @Override
    public Material.Baked particleMaterial(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        return this.resolveParticleMaterial(this.delegate.particleMaterial(level, pos, state));
    }
    
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        return this.delegate.materialFlags(level, pos, state);
    }
    
    @Override
    public Material.Baked particleMaterial() {
        return this.particleMaterial;
    }
    
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.materialFlags;
    }
    
    ///
    /// Resolves the connection masks for block at the given position in the given world with the given state.
    ///
    /// @param level block level
    /// @param pos   block position
    /// @param state block state
    ///
    /// @return connection masks for the block
    ///
    private ConnectionMasks resolveConnections(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        // No layers at all
        if (this.layers.isEmpty()) {
            return ConnectionMasks.empty();
        }
        
        // Single layer
        if (this.layers.size() == 1) {
            final ConnectedTextureLayer connection = this.layers.getFirst();
            return ConnectionMasks.single(connection.texture().sprite(), ConnectionFaceMasks.resolve(level, pos, state, connection.rule()));
        }
        
        // Multiple layers
        final HashMap<Identifier, ConnectionFaceMasks> resolved = new HashMap<>(this.layers.size());
        
        for (final ConnectedTextureLayer connection : this.layers) {
            resolved.put(connection.texture().sprite(), ConnectionFaceMasks.resolve(level, pos, state, connection.rule()));
        }
        
        return ConnectionMasks.multi(Map.copyOf(resolved));
    }
    
    ///
    /// Resolves the particle material for the given baked material.
    /// Since connected textures are stores as an atlas texture by default, the particle
    /// will use the entire atlas as sprite. Here we remap the particle to only use the origin
    /// texture as sprite to ensure that the particles line up correctly with how the block looks alone
    ///
    /// @param material baked material
    ///
    /// @return resolved particle material
    ///
    private Material.Baked resolveParticleMaterial(final Material.Baked material) {
        return this.shouldRemapParticle(material.sprite()) ? ConnectedTextureUvResolver.remap(material, SpriteSheet.Pos.ORIGIN) : material;
    }
    
    ///
    /// Determines whether the given sprite should be remapped to the origin texture.
    /// If the current sprite is one of the atlas sprites of the {@link ConnectedTextureBlockModel#layers},
    /// then we should remap.
    ///
    /// @param sprite sprite to check
    ///
    /// @return `true` if the sprite should be remapped, `false` otherwise
    ///
    private boolean shouldRemapParticle(final TextureAtlasSprite sprite) {
        if (this.layers.isEmpty()) return true;
        final Identifier spriteId = sprite.contents().name();
        
        for (final ConnectedTextureLayer connection : this.layers) {
            if (connection.texture().sprite().equals(spriteId)) {
                return true;
            }
        }
        
        return false;
    }
    
    ///
    /// Unbaked block state model used to bake connected texture block models.
    ///
    /// @param model       base unbaked blockstate model
    /// @param connections explicit connected texture bindings
    ///
    public record Unbaked(BlockStateModel.Unbaked model, List<ConnectedTextureLayer> connections) implements CustomUnbakedBlockStateModel {
        
        ///
        /// Codec used to serialize unbaked connected texture block models.
        ///
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("model").forGetter(Unbaked::model),
                ConnectedTextureLayer.CODEC.listOf().optionalFieldOf("connections", List.of()).forGetter(Unbaked::connections)
        ).apply(instance, Unbaked::new));
        
        ///
        /// Creates an unbaked connected texture model that only uses a predicate.
        ///
        /// @param model base unbaked blockstate model
        ///
        public Unbaked(final BlockStateModel.Unbaked model) {
            this(model, List.of());
        }
        
        ///
        /// Normalizes the connection list and validates that at least one connection strategy is present.
        ///
        /// @param model       base unbaked blockstate model
        /// @param connections explicit connected texture bindings
        ///
        public Unbaked {
            if (connections.isEmpty()) throw new IllegalArgumentException("Connected texture model requires at least one connection");
            connections = List.copyOf(connections);
        }
        
        @Override
        public BlockStateModel bake(final ModelBaker baker) {
            return new ConnectedTextureBlockModel(this.model.bake(baker), this.connections);
        }
        
        @Override
        public void resolveDependencies(final ResolvableModel.Resolver resolver) {
            this.model.resolveDependencies(resolver);
        }
        
        @Override
        public MapCodec<Unbaked> codec() {
            return ConnectedTextureBlockModel.Unbaked.MAP_CODEC;
        }
    }
    
    private record GeometryKey(@Nullable Object delegate, ConnectionMasks connections) { }
    
    @SuppressWarnings("deprecation")
    private static final class ConnectedTexturePart implements BlockStateModelPart {
        
        private final BlockStateModelPart delegate;
        private final ConnectionMasks spriteMasks;
        private final EnumMap<Direction, List<BakedQuad>> sidedQuads = new EnumMap<>(Direction.class);
        private @Nullable List<BakedQuad> unsidedQuads;
        
        private ConnectedTexturePart(final BlockStateModelPart delegate, final ConnectionMasks spriteMasks) {
            this.delegate = delegate;
            this.spriteMasks = spriteMasks;
        }
        
        @Override
        public List<BakedQuad> getQuads(final @Nullable Direction direction) {
            if (direction != null) return this.sidedQuads.computeIfAbsent(direction, side -> this.remap(this.delegate.getQuads(side), side));
            if (this.unsidedQuads == null) this.unsidedQuads = this.remap(this.delegate.getQuads(null), null);
            return this.unsidedQuads;
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
            if (quads.isEmpty()) return quads;
            final List<BakedQuad> remapped = new ArrayList<>(quads.size());
            
            for (final BakedQuad quad : quads) {
                final Direction face = direction != null ? direction : quad.direction();
                final ConnectionFaceMasks masks = this.spriteMasks.get(quad.materialInfo().sprite().contents().name());
                remapped.add(masks != null ? ConnectedTextureUvResolver.remap(quad, SpriteSheet.resolve(masks.mask(face))) : quad);
            }
            
            return List.copyOf(remapped);
        }
    }
}
