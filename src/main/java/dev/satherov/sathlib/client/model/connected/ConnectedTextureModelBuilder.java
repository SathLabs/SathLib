package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class ConnectedTextureModelBuilder {
    
    private ConnectedTextureModelBuilder() { }
    
    public static void registerCubeAll(final BlockModelGenerators generators, final Block block, final Identifier predicate) {
        final Material texture = TextureMapping.getBlockTexture(block);
        final Identifier model = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block), generators.modelOutput);
        final Identifier itemModel = ConnectedTextureModelBuilder.createItemModel(generators, block, model, texture);
        
        generators.blockStateOutput.accept(ConnectedTextureModelBuilder.generator(block, model, predicate));
        generators.registerSimpleItemModel(block, itemModel);
    }
    
    public static void registerCubeAll(
            final BlockModelGenerators generators,
            final Block block,
            final Material texture,
            final Identifier predicate
    ) {
        final Identifier model = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(texture), generators.modelOutput);
        final Identifier itemModel = ConnectedTextureModelBuilder.createItemModel(generators, block, model, texture);
        
        generators.blockStateOutput.accept(ConnectedTextureModelBuilder.generator(block, model, predicate));
        generators.registerSimpleItemModel(block, itemModel);
    }
    
    public static void register(
            final BlockModelGenerators generators,
            final Block block,
            final TexturedModel.Provider provider,
            final Identifier predicate
    ) {
        ConnectedTextureModelBuilder.register(generators, block, provider.get(block), predicate);
    }
    
    public static void register(
            final BlockModelGenerators generators,
            final Block block,
            final TexturedModel texturedModel,
            final Identifier predicate
    ) {
        final Identifier model = texturedModel.create(block, generators.modelOutput);
        ConnectedTextureModelBuilder.register(generators, block, model, predicate);
    }
    
    public static void register(
            final BlockModelGenerators generators,
            final Block block,
            final ModelTemplate template,
            final TextureMapping textures,
            final Identifier predicate
    ) {
        final Identifier model = template.create(block, textures, generators.modelOutput);
        ConnectedTextureModelBuilder.register(generators, block, model, predicate);
    }
    
    public static void register(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier model,
            final Identifier predicate
    ) {
        generators.blockStateOutput.accept(ConnectedTextureModelBuilder.generator(block, model, predicate));
        generators.registerSimpleItemModel(block, model);
    }
    
    public static Identifier createItemModel(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier baseModel,
            final Material texture
    ) {
        return ConnectedTextureItemModel.create(generators, block, baseModel, texture);
    }
    
    public static Identifier createItemModel(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier baseModel,
            final Identifier texture
    ) {
        return ConnectedTextureModelBuilder.createItemModel(generators, block, baseModel, new Material(texture));
    }
    
    public static BlockModelDefinitionGenerator generator(
            final Block block,
            final Identifier model,
            final Identifier predicate
    ) {
        return MultiVariantGenerator.dispatch(block, ConnectedTextureModelBuilder.variant(model, predicate));
    }
    
    public static MultiVariant variant(final Identifier model, final Identifier predicate) {
        return MultiVariant.of(ConnectedTextureModelBuilder.builder(model, predicate));
    }
    
    public static SLConnectedTextureModelBuilder connected(final Variant variant) {
        return SLConnectedTextureModelBuilder.connected(variant);
    }
    
    public static SLConnectedTextureModelBuilder connected(final Identifier model) {
        return SLConnectedTextureModelBuilder.connected(model);
    }
    
    public static CustomBlockStateModelBuilder builder(final Identifier model, final Identifier predicate) {
        return ConnectedTextureModelBuilder.builder(new Variant(model), predicate);
    }
    
    public static CustomBlockStateModelBuilder builder(final Variant variant, final Identifier predicate) {
        return new ConnectedBlockStateBuilder(new SingleVariant.Unbaked(variant), predicate);
    }
    
    @NothingNull
    private static final class ConnectedBlockStateBuilder extends CustomBlockStateModelBuilder {
        
        private final BlockStateModel.Unbaked model;
        private final Identifier predicate;
        
        private ConnectedBlockStateBuilder(final BlockStateModel.Unbaked model, final Identifier predicate) {
            this.model = model;
            this.predicate = predicate;
        }
        
        @Override
        public CustomBlockStateModelBuilder with(final VariantMutator variantMutator) {
            if (this.model instanceof SingleVariant.Unbaked(Variant variant)) {
                return new ConnectedBlockStateBuilder(new SingleVariant.Unbaked(variant.with(variantMutator)), this.predicate);
            }
            
            return this;
        }
        
        @Override
        public CustomBlockStateModelBuilder with(final UnbakedMutator variantMutator) {
            return new ConnectedBlockStateBuilder(variantMutator.apply(this.model), this.predicate);
        }
        
        @Override
        public CustomUnbakedBlockStateModel toUnbaked() {
            return new ConnectedTextureBlockModel.Unbaked(this.model, this.predicate);
        }
    }
}
