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

///
/// Utility methods for generating connected texture models during blockstate data generation.
///
public final class ConnectedTextureModelBuilder {
    
    private ConnectedTextureModelBuilder() { }
    
    ///
    /// Registers a cube-all block model and matching connected-texture item model.
    ///
    /// @param generators data generators receiving the models
    /// @param block      block to generate models for
    /// @param predicate  predicate identifier used by the connected texture model
    ///
    public static void registerCubeAll(final BlockModelGenerators generators, final Block block, final Identifier predicate) {
        final Material texture = TextureMapping.getBlockTexture(block);
        final Identifier model = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block), generators.modelOutput);
        final Identifier itemModel = ConnectedTextureModelBuilder.createItemModel(generators, block, model, texture);
        
        generators.blockStateOutput.accept(ConnectedTextureModelBuilder.generator(block, model, predicate));
        generators.registerSimpleItemModel(block, itemModel);
    }
    
    ///
    /// Registers a cube-all block model and matching connected-texture item model with an explicit texture.
    ///
    /// @param generators data generators receiving the models
    /// @param block      block to generate models for
    /// @param texture    texture used by the generated cube-all model
    /// @param predicate  predicate identifier used by the connected texture model
    ///
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
    
    ///
    /// Registers connected texture models using a textured-model provider.
    ///
    /// @param generators data generators receiving the models
    /// @param block      block to generate models for
    /// @param provider   textured-model provider used to create the base model
    /// @param predicate  predicate identifier used by the connected texture model
    ///
    public static void register(
            final BlockModelGenerators generators,
            final Block block,
            final TexturedModel.Provider provider,
            final Identifier predicate
    ) {
        ConnectedTextureModelBuilder.register(generators, block, provider.get(block), predicate);
    }
    
    ///
    /// Registers connected texture models using a textured model instance.
    ///
    /// @param generators    data generators receiving the models
    /// @param block         block to generate models for
    /// @param texturedModel textured model used to create the base model
    /// @param predicate     predicate identifier used by the connected texture model
    ///
    public static void register(
            final BlockModelGenerators generators,
            final Block block,
            final TexturedModel texturedModel,
            final Identifier predicate
    ) {
        final Identifier model = texturedModel.create(block, generators.modelOutput);
        ConnectedTextureModelBuilder.register(generators, block, model, predicate);
    }
    
    ///
    /// Registers connected texture models using an explicit model template and texture mapping.
    ///
    /// @param generators data generators receiving the models
    /// @param block      block to generate models for
    /// @param template   model template used to create the base model
    /// @param textures   texture mapping supplied to the template
    /// @param predicate  predicate identifier used by the connected texture model
    ///
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
    
    ///
    /// Registers connected texture blockstate output using an existing model identifier.
    ///
    /// @param generators data generators receiving the models
    /// @param block      block to generate models for
    /// @param model      base model identifier
    /// @param predicate  predicate identifier used by the connected texture model
    ///
    public static void register(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier model,
            final Identifier predicate
    ) {
        generators.blockStateOutput.accept(ConnectedTextureModelBuilder.generator(block, model, predicate));
        generators.registerSimpleItemModel(block, model);
    }
    
    ///
    /// Creates the item model used for a connected texture block.
    ///
    /// @param generators data generators receiving the model
    /// @param block      block to generate the item model for
    /// @param baseModel  base model identifier used as the parent
    /// @param texture    texture atlas material used for the connected texture
    ///
    /// @return generated item model identifier
    ///
    public static Identifier createItemModel(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier baseModel,
            final Material texture
    ) {
        return ConnectedTextureItemModel.create(generators, block, baseModel, texture);
    }
    
    ///
    /// Creates the item model used for a connected texture block.
    ///
    /// @param generators data generators receiving the model
    /// @param block      block to generate the item model for
    /// @param baseModel  base model identifier used as the parent
    /// @param texture    texture atlas identifier used for the connected texture
    ///
    /// @return generated item model identifier
    ///
    public static Identifier createItemModel(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier baseModel,
            final Identifier texture
    ) {
        return ConnectedTextureModelBuilder.createItemModel(generators, block, baseModel, new Material(texture));
    }
    
    ///
    /// Creates a blockstate generator that wraps the supplied model in a connected texture variant.
    ///
    /// @param block     block owning the generated blockstate definition
    /// @param model     base model identifier
    /// @param predicate predicate identifier used by the connected texture model
    ///
    /// @return blockstate definition generator
    ///
    public static BlockModelDefinitionGenerator generator(
            final Block block,
            final Identifier model,
            final Identifier predicate
    ) {
        return MultiVariantGenerator.dispatch(block, ConnectedTextureModelBuilder.variant(model, predicate));
    }
    
    ///
    /// Creates a connected texture multi-variant using the supplied base model.
    ///
    /// @param model     base model identifier
    /// @param predicate predicate identifier used by the connected texture model
    ///
    /// @return connected texture multi-variant
    ///
    public static MultiVariant variant(final Identifier model, final Identifier predicate) {
        return MultiVariant.of(ConnectedTextureModelBuilder.builder(model, predicate));
    }
    
    ///
    /// Starts a connected texture model builder from a variant.
    ///
    /// @param variant base variant to wrap
    ///
    /// @return connected texture model builder
    ///
    public static SLConnectedTextureModelBuilder connected(final Variant variant) {
        return SLConnectedTextureModelBuilder.connected(variant);
    }
    
    ///
    /// Starts a connected texture model builder from a model identifier.
    ///
    /// @param model base model identifier
    ///
    /// @return connected texture model builder
    ///
    public static SLConnectedTextureModelBuilder connected(final Identifier model) {
        return SLConnectedTextureModelBuilder.connected(model);
    }
    
    ///
    /// Creates a blockstate builder for a single base model.
    ///
    /// @param model     base model identifier
    /// @param predicate predicate identifier used by the connected texture model
    ///
    /// @return connected texture blockstate builder
    ///
    public static CustomBlockStateModelBuilder builder(final Identifier model, final Identifier predicate) {
        return ConnectedTextureModelBuilder.builder(new Variant(model), predicate);
    }
    
    ///
    /// Creates a blockstate builder for a single base variant.
    ///
    /// @param variant   base variant to wrap
    /// @param predicate predicate identifier used by the connected texture model
    ///
    /// @return connected texture blockstate builder
    ///
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
