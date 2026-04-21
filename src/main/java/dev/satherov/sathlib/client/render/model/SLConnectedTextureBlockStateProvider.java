package dev.satherov.sathlib.client.render.model;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;

/// Model-provider base class with helpers for generating connected-texture
/// blockstates.
///
/// Subclasses register connected-texture models explicitly from
/// {@link #registerModels(BlockModelGenerators, ItemModelGenerators)}.
/// This provider does not scan registries or infer block participation.
///
/// For a simple connected-texture block that uses one atlas and the default
/// same-block connection rule, a subclass typically looks like this:
///
/// ```java
/// public final class ExampleModels extends SLConnectedTextureBlockStateProvider {
///     public ExampleModels(PackOutput output) {
///         super(output, "examplemod");
///     }
///
///     @Override
///     protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
///         this.connectedCubeAll(blockModels, ExampleBlocks.MARBLE_BRICKS.value());
///     }
/// }
/// ```
///
/// If a block should vary the displayed connected-texture atlas based on
/// predicates, use {@link #predicateConnectedCubeAll(BlockModelGenerators,
/// Block, Identifier, Map, List, SLConnectedTextureRule)} and provide ordered
/// selectors:
///
/// ```java
/// public final class ExampleModels extends SLConnectedTextureBlockStateProvider {
///     public ExampleModels(PackOutput output) {
///         super(output, "examplemod");
///     }
///
///     @Override
///     protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
///         this.predicateConnectedCubeAll(
///                 blockModels,
///                 ExampleBlocks.RUNE_GLASS.value(),
///                 ExampleTextures.RUNE_GLASS_BASE,
///                 Map.of(
///                         SLConnectedTextureTarget.DEFAULT_VARIANT, ExampleTextures.RUNE_GLASS_BASE,
///                         "active", ExampleTextures.RUNE_GLASS_ACTIVE),
///                 List.of(SLConnectedTextureVariantSelector.of(
///                         "active",
///                         SLConnectedTextureRules.stateValue(ExampleBlockStateProperties.ACTIVE, true))),
///                 SLConnectedTextureRules.and(
///                         SLConnectedTextureRules.sameBlock(),
///                         SLConnectedTextureRules.not(
///                                 SLConnectedTextureRules.stateValue(ExampleBlockStateProperties.CRACKED, true))));
///     }
/// }
/// ```
///
/// When the helper methods are too limited, subclasses can bypass them and
/// write directly to {@code blockModels.blockStateOutput} with
/// {@link SLConnectedTextureModelBuilder} or
/// {@link SLPredicateConnectedTextureModelBuilder}.
public abstract class SLConnectedTextureBlockStateProvider extends ModelProvider {
    
    /// Creates a connected-texture blockstate provider.
    ///
    /// @param output pack output target
    /// @param modId  namespace used by the provider
    protected SLConnectedTextureBlockStateProvider(PackOutput output, String modId) {
        super(output, modId);
    }
    
    /// Registers models for this provider.
    ///
    /// @param blockModels block model generator access
    /// @param itemModels  item model generator access
    @Override
    protected abstract void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels);
    
    /// Generates a cube-all connected-texture model using the block's default
    /// texture and the default same-block connection rule.
    ///
    /// @param blockModels block model generator access
    /// @param block       target block
    protected void connectedCubeAll(BlockModelGenerators blockModels, Block block) {
        this.connectedCubeAll(blockModels, block, TextureMapping.getBlockTexture(block).sprite());
    }
    
    /// Generates a cube-all connected-texture model using the supplied texture
    /// and the default same-block connection rule.
    ///
    /// @param blockModels block model generator access
    /// @param block       target block
    /// @param texture     connected-texture atlas
    protected void connectedCubeAll(BlockModelGenerators blockModels, Block block, Identifier texture) {
        this.connectedCubeAll(blockModels, block, texture, SLConnectedTextureRules.sameBlock());
    }
    
    /// Generates a cube-all connected-texture model using the supplied
    /// connection rule.
    ///
    /// @param blockModels    block model generator access
    /// @param block          target block
    /// @param texture        connected-texture atlas
    /// @param connectionRule rule controlling connectivity between neighbors
    protected void connectedCubeAll(
            BlockModelGenerators blockModels,
            Block block,
            Identifier texture,
            SLConnectedTextureRule connectionRule
    ) {
        Identifier baseModel = TexturedModel.createAllSame(new Material(texture)).create(block, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(
                block,
                MultiVariant.of(SLConnectedTextureModelBuilder.of(baseModel).target("main", texture, connectionRule))));
    }
    
    /// Generates a predicate-aware cube-all connected-texture model using the
    /// default same-block connection rule.
    ///
    /// @param blockModels  block model generator access
    /// @param block        target block
    /// @param matchTexture base texture matched against source quads
    /// @param variants     atlas textures keyed by variant id
    protected void predicateConnectedCubeAll(
            BlockModelGenerators blockModels,
            Block block,
            Identifier matchTexture,
            Map<String, Identifier> variants
    ) {
        this.predicateConnectedCubeAll(blockModels, block, matchTexture, variants, List.of(), SLConnectedTextureRules.sameBlock());
    }
    
    /// Generates a predicate-aware cube-all connected-texture model.
    ///
    /// @param blockModels    block model generator access
    /// @param block          target block
    /// @param matchTexture   base texture matched against source quads
    /// @param variants       atlas textures keyed by variant id
    /// @param selectors      ordered selectors resolving the active variant
    /// @param connectionRule rule controlling connectivity between neighbors
    protected void predicateConnectedCubeAll(
            BlockModelGenerators blockModels,
            Block block,
            Identifier matchTexture,
            Map<String, Identifier> variants,
            List<SLConnectedTextureVariantSelector> selectors,
            SLConnectedTextureRule connectionRule
    ) {
        Identifier baseModel = TexturedModel.createAllSame(new Material(matchTexture)).create(block, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(
                block,
                MultiVariant.of(SLPredicateConnectedTextureModelBuilder.of(baseModel)
                        .target("main", matchTexture, variants, selectors, connectionRule))));
    }
}
