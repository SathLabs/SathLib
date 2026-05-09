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

@NothingNull
@SuppressWarnings("deprecation")
public class ConditionalBlockModel implements DynamicBlockStateModel {
    
    public static final Identifier ID = SathLib.id("conditional");
    
    private final BlockStateModel fallback;
    private final List<Case> cases;
    private final Material.Baked particleMaterial;
    private final @BakedQuad.MaterialFlags int materialFlags;
    
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
    
    private record Selection(int index, BlockStateModel model) { }
    
    private record GeometryKey(int index, BlockStateModel model, @Nullable Object delegateKey) { }
    
    public record Case(ConditionalPredicate predicate, BlockStateModel model) { }
    
    public record UnbakedCase(ConditionalPredicate predicate, BlockStateModel.Unbaked model) {
        
        private static final Codec<UnbakedCase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ConditionalRules.CODEC.fieldOf("predicate").forGetter(UnbakedCase::predicate),
                Unbaked.CODEC.fieldOf("model").forGetter(UnbakedCase::model)
        ).apply(instance, UnbakedCase::new));
        
        private ConditionalBlockModel.Case bake(final ModelBaker baker) {
            return new Case(this.predicate, this.model.bake(baker));
        }
        
        private void resolveDependencies(final ResolvableModel.Resolver resolver) {
            this.model.resolveDependencies(resolver);
        }
    }
    
    public record Unbaked(BlockStateModel.Unbaked fallback, List<ConditionalBlockModel.UnbakedCase> cases) implements CustomUnbakedBlockStateModel {
        
        public static final MapCodec<ConditionalBlockModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("fallback").forGetter(ConditionalBlockModel.Unbaked::fallback),
                ConditionalBlockModel.UnbakedCase.CODEC.listOf().optionalFieldOf("cases", List.of()).forGetter(ConditionalBlockModel.Unbaked::cases)
        ).apply(instance, ConditionalBlockModel.Unbaked::new));
        
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
