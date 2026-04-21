package dev.satherov.sathlib.client.render.model;

import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Datagen builder for the predicate-aware connected-texture blockstate model.
///
/// This builder extends the plain connected-texture model by allowing each
/// target to select one of several texture atlases through ordered
/// {@link SLConnectedTextureVariantSelector variant selectors}. Connectivity is
/// still handled through the same serialized rule tree.
public class SLPredicateConnectedTextureModelBuilder extends CustomBlockStateModelBuilder {
    
    private final Variant baseModel;
    private final List<SLConnectedTextureTarget> targets;
    
    private SLPredicateConnectedTextureModelBuilder(Variant baseModel, List<SLConnectedTextureTarget> targets) {
        this.baseModel = baseModel;
        this.targets = List.copyOf(targets);
    }
    
    /// Starts a builder from a base model id.
    ///
    /// The referenced model is wrapped as a single blockstate variant before
    /// any connected-texture targets are attached.
    ///
    /// @param baseModel base block model location
    ///
    /// @return model builder
    public static SLPredicateConnectedTextureModelBuilder of(Identifier baseModel) {
        return SLPredicateConnectedTextureModelBuilder.of(new Variant(baseModel));
    }
    
    /// Starts a builder from a base variant.
    ///
    /// Use this when the base variant already carries rotations, UV locking,
    /// or other variant-level configuration.
    ///
    /// @param baseModel base variant
    ///
    /// @return model builder
    public static SLPredicateConnectedTextureModelBuilder of(Variant baseModel) {
        return new SLPredicateConnectedTextureModelBuilder(baseModel, List.of());
    }
    
    /// Adds a target with default selectors and the default same-block
    /// connection rule on all faces.
    ///
    /// The first matching variant will be chosen at runtime. With this
    /// overload, no selectors are registered, so the target falls back to its
    /// default variant texture.
    ///
    /// @param id           target id
    /// @param matchTexture base texture matched against source quads
    /// @param variants     atlas textures by variant id
    ///
    /// @return updated builder
    public SLPredicateConnectedTextureModelBuilder target(String id, Identifier matchTexture, Map<String, Identifier> variants) {
        return this.target(id, matchTexture, variants, List.of(), SLConnectedTextureRules.sameBlock(), EnumSet.allOf(Direction.class));
    }
    
    /// Adds a target with selectors and the default same-block connection rule
    /// on all faces.
    ///
    /// Selectors are evaluated in list order. The first selector whose
    /// predicate matches the current block context wins.
    ///
    /// @param id           target id
    /// @param matchTexture base texture matched against source quads
    /// @param variants     atlas textures by variant id
    /// @param selectors    ordered selectors resolving the active variant
    ///
    /// @return updated builder
    public SLPredicateConnectedTextureModelBuilder target(
            String id,
            Identifier matchTexture,
            Map<String, Identifier> variants,
            List<SLConnectedTextureVariantSelector> selectors
    ) {
        return this.target(id, matchTexture, variants, selectors, SLConnectedTextureRules.sameBlock(), EnumSet.allOf(Direction.class));
    }
    
    /// Adds a target with selectors and the default same-block connection rule.
    ///
    /// If no faces are supplied, the target applies to every face.
    ///
    /// @param id           target id
    /// @param matchTexture base texture matched against source quads
    /// @param variants     atlas textures by variant id
    /// @param selectors    ordered selectors resolving the active variant
    /// @param faces        faces using the atlas
    ///
    /// @return updated builder
    public SLPredicateConnectedTextureModelBuilder target(
            String id,
            Identifier matchTexture,
            Map<String, Identifier> variants,
            List<SLConnectedTextureVariantSelector> selectors,
            Direction... faces
    ) {
        EnumSet<Direction> resolvedFaces = faces.length == 0 ? EnumSet.allOf(Direction.class) : EnumSet.copyOf(Arrays.asList(faces));
        return this.target(id, matchTexture, variants, selectors, SLConnectedTextureRules.sameBlock(), resolvedFaces);
    }
    
    /// Adds a target with selectors and a custom connection rule.
    ///
    /// This overload accepts an explicit {@link EnumSet} when the caller
    /// already has a resolved face set.
    ///
    /// @param id           target id
    /// @param matchTexture base texture matched against source quads
    /// @param variants     atlas textures by variant id
    /// @param selectors    ordered selectors resolving the active variant
    /// @param rule         serialized connection rule
    /// @param faces        faces using the atlas
    ///
    /// @return updated builder
    public SLPredicateConnectedTextureModelBuilder target(
            String id,
            Identifier matchTexture,
            Map<String, Identifier> variants,
            List<SLConnectedTextureVariantSelector> selectors,
            SLConnectedTextureRule rule,
            EnumSet<Direction> faces
    ) {
        List<SLConnectedTextureTarget> updatedTargets = new ArrayList<>(this.targets);
        updatedTargets.add(new SLConnectedTextureTarget(
                id,
                matchTexture,
                Map.copyOf(new LinkedHashMap<>(variants)),
                selectors,
                rule,
                faces,
                SLBlob47ConnectedTextureLayout.INSTANCE,
                SLConnectedTextureMasks.NONE));
        return new SLPredicateConnectedTextureModelBuilder(this.baseModel, updatedTargets);
    }
    
    /// Adds a target with selectors and a custom connection rule on all faces.
    ///
    /// This is equivalent to passing every {@link Direction} explicitly.
    ///
    /// @param id           target id
    /// @param matchTexture base texture matched against source quads
    /// @param variants     atlas textures by variant id
    /// @param selectors    ordered selectors resolving the active variant
    /// @param rule         serialized connection rule
    ///
    /// @return updated builder
    public SLPredicateConnectedTextureModelBuilder target(
            String id,
            Identifier matchTexture,
            Map<String, Identifier> variants,
            List<SLConnectedTextureVariantSelector> selectors,
            SLConnectedTextureRule rule
    ) {
        return this.target(id, matchTexture, variants, selectors, rule, EnumSet.allOf(Direction.class));
    }
    
    /// Adds a target with selectors and a custom connection rule.
    ///
    /// This overload accepts varargs for convenience. An empty vararg means
    /// the target applies to all faces.
    ///
    /// @param id           target id
    /// @param matchTexture base texture matched against source quads
    /// @param variants     atlas textures by variant id
    /// @param selectors    ordered selectors resolving the active variant
    /// @param rule         serialized connection rule
    /// @param faces        faces using the atlas
    ///
    /// @return updated builder
    public SLPredicateConnectedTextureModelBuilder target(
            String id,
            Identifier matchTexture,
            Map<String, Identifier> variants,
            List<SLConnectedTextureVariantSelector> selectors,
            SLConnectedTextureRule rule,
            Direction... faces
    ) {
        EnumSet<Direction> resolvedFaces = faces.length == 0 ? EnumSet.allOf(Direction.class) : EnumSet.copyOf(Arrays.asList(faces));
        return this.target(id, matchTexture, variants, selectors, rule, resolvedFaces);
    }
    
    /// Applies a variant mutator to the wrapped base model while preserving all
    /// configured connected-texture targets.
    ///
    /// @param variantMutator mutator applied to the base variant
    ///
    /// @return updated builder
    @Override
    public SLPredicateConnectedTextureModelBuilder with(VariantMutator variantMutator) {
        return new SLPredicateConnectedTextureModelBuilder(this.baseModel.with(variantMutator), this.targets);
    }
    
    /// Applies an unbaked mutator to the wrapped base model.
    ///
    /// Only single-variant results are supported. Returning any other unbaked
    /// model shape will fail fast with an
    /// {@link UnsupportedOperationException}.
    ///
    /// @param variantMutator mutator applied to the base variant
    ///
    /// @return updated builder
    @Override
    public SLPredicateConnectedTextureModelBuilder with(UnbakedMutator variantMutator) {
        BlockStateModel.Unbaked mutated = variantMutator.apply(new SingleVariant.Unbaked(this.baseModel));
        if (mutated instanceof SingleVariant.Unbaked(Variant variant)) {
            return new SLPredicateConnectedTextureModelBuilder(variant, this.targets);
        }
        throw new UnsupportedOperationException("Connected-texture builders only support single-variant base models");
    }
    
    /// Converts the builder into the serializable unbaked model definition used
    /// by datagen output.
    ///
    /// @return unbaked predicate-aware connected-texture model
    @Override
    public SLPredicateConnectedTextureModel toUnbaked() {
        return new SLPredicateConnectedTextureModel(this.baseModel, this.targets);
    }
}
