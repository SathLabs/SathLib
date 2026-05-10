package dev.satherov.sathlib.client.model.conditional;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jspecify.annotations.Nullable;

import java.util.List;

///
/// Conditional block model that selects a block state model based on a predicate.
///
/// @see ConditionalPredicate
///
@NothingNull
@SuppressWarnings("deprecation")
public class ConditionalBlockModel implements DynamicBlockStateModel {
    
    ///
    /// The identifier of the conditional block state model.
    ///
    public static final Identifier ID = SathLib.id("conditional");
    
    private final BlockStateModel fallback;
    private final List<Case> cases;
    private final Material.Baked particleMaterial;
    private final @BakedQuad.MaterialFlags int materialFlags;
    
    ///
    /// Creates a conditional block model.
    ///
    /// @param fallback the fallback model to select when no case matches
    /// @param cases    the list of cases to select from
    ///
    public ConditionalBlockModel(final BlockStateModel fallback, final List<Case> cases) {
        this.fallback = fallback;
        this.cases = List.copyOf(cases);
        this.particleMaterial = fallback.particleMaterial();
        this.materialFlags = fallback.materialFlags();
    }
    
    @Override
    public void collectParts(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final RandomSource random,
            final List<BlockStateModelPart> parts
    ) {
        this.selectModel(level, pos, state).collectParts(level, pos, state, random, parts);
    }
    
    @Override
    public Object createGeometryKey(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final RandomSource random
    ) {
        final Selection selection = this.select(level, pos, state);
        return new GeometryKey(selection.index(), selection.model(), selection.model().createGeometryKey(level, pos, state, random));
    }
    
    @Override
    public Material.Baked particleMaterial() {
        return this.particleMaterial;
    }
    
    @Override
    public Material.Baked particleMaterial(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        return this.selectModel(level, pos, state).particleMaterial(level, pos, state);
    }
    
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.materialFlags;
    }
    
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        return this.selectModel(level, pos, state).materialFlags(level, pos, state);
    }
    
    private BlockStateModel selectModel(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        return this.select(level, pos, state).model();
    }
    
    ///
    /// Selects a model based on the given level, position, and state.
    ///
    /// @param level the rendering level
    /// @param pos   the block position
    /// @param state the block state
    ///
    /// @return the selected model
    ///
    private Selection select(final BlockAndTintGetter level, final BlockPos pos, final BlockState state) {
        final ModelData modelData = level.getModelData(pos);
        
        for (int i = 0; i < this.cases.size(); i++) {
            final Case candidate = this.cases.get(i);
            if (candidate.predicate().matches(level, pos, state, modelData)) {
                return new Selection(i, candidate.model());
            }
        }
        
        return new Selection(-1, this.fallback);
    }
    
    ///
    /// Represents a selected model and its index.
    ///
    /// @param index the index of the selected model
    /// @param model the selected model
    ///
    private record Selection(int index, BlockStateModel model) { }
    
    ///
    /// Represents a geometry key for model caching.
    ///
    /// @param index       the index of the model
    /// @param model       the model
    /// @param delegateKey the delegate key for the model
    ///
    private record GeometryKey(int index, BlockStateModel model, @Nullable Object delegateKey) { }
    
    ///
    /// Represents a case in a conditional block model.
    ///
    /// @param predicate the predicate to match
    /// @param model     the model to select when the predicate matches
    ///
    public record Case(ConditionalPredicate predicate, BlockStateModel model) { }
    
    ///
    /// Represents a case in a conditional block model that is unbaked.
    ///
    /// @param predicate the predicate to match
    /// @param model     the model to select when the predicate matches
    ///
    public record UnbakedCase(ConditionalPredicate predicate, BlockStateModel.Unbaked model) {
        
        ///
        /// Codec for unbaked cases.
        ///
        private static final Codec<UnbakedCase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ConditionalRules.CODEC.fieldOf("predicate").forGetter(UnbakedCase::predicate),
                Unbaked.CODEC.fieldOf("model").forGetter(UnbakedCase::model)
        ).apply(instance, UnbakedCase::new));
        
        ///
        /// Bakes the case into a {@link Case}.
        ///
        /// @param baker the model baker
        ///
        /// @return the baked case
        ///
        private Case bake(final ModelBaker baker) {
            return new Case(this.predicate, this.model.bake(baker));
        }
        
        ///
        /// Resolves the case's dependencies.
        ///
        /// @param resolver the model resolver
        ///
        private void resolveDependencies(final ResolvableModel.Resolver resolver) {
            this.model.resolveDependencies(resolver);
        }
    }
    
    ///
    /// Represents an unbaked conditional block model.
    ///
    /// @param fallback the unbaked fallback model
    /// @param cases    the list of unbaked cases
    ///
    public record Unbaked(BlockStateModel.Unbaked fallback, List<ConditionalBlockModel.UnbakedCase> cases) implements CustomUnbakedBlockStateModel {
        
        ///
        /// Codec for unbaked conditional block models.
        ///
        public static final MapCodec<ConditionalBlockModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("fallback").forGetter(ConditionalBlockModel.Unbaked::fallback),
                ConditionalBlockModel.UnbakedCase.CODEC.listOf().optionalFieldOf("cases", List.of()).forGetter(ConditionalBlockModel.Unbaked::cases)
        ).apply(instance, ConditionalBlockModel.Unbaked::new));
        
        ///
        /// Copies the list of cases.
        ///
        public Unbaked {
            cases = List.copyOf(cases);
        }
        
        @Override
        public BlockStateModel bake(final ModelBaker baker) {
            return new ConditionalBlockModel(
                    this.fallback.bake(baker),
                    this.cases.stream().map(candidate -> candidate.bake(baker)).toList()
            );
        }
        
        @Override
        public void resolveDependencies(final ResolvableModel.Resolver resolver) {
            this.fallback.resolveDependencies(resolver);
            this.cases.forEach(candidate -> candidate.resolveDependencies(resolver));
        }
        
        @Override
        public MapCodec<Unbaked> codec() {
            return Unbaked.MAP_CODEC;
        }
    }
}
