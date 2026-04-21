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
import java.util.List;
import java.util.Map;

/// Datagen builder for SathLib's simple connected-texture blockstate model.
///
/// This builder is intended for targets that always render a single atlas and
/// only vary by connectivity. Connection behavior is configured per target using
/// {@link SLConnectedTextureRule} instances instead of requiring the block to
/// implement any dedicated interface.
public class SLConnectedTextureModelBuilder extends CustomBlockStateModelBuilder {
    
    private final Variant baseModel;
    private final List<SLConnectedTextureTarget> targets;
    
    private SLConnectedTextureModelBuilder(Variant baseModel, List<SLConnectedTextureTarget> targets) {
        this.baseModel = baseModel;
        this.targets = List.copyOf(targets);
    }
    
    /// Starts a builder from a base model id.
    ///
    /// The referenced model is wrapped as a single NeoForge blockstate
    /// variant, after which connected-texture targets can be attached with
    /// {@link #target(String, Identifier, SLConnectedTextureRule, Direction...)}.
    ///
    /// @param baseModel base block model location
    ///
    /// @return model builder
    public static SLConnectedTextureModelBuilder of(Identifier baseModel) {
        return SLConnectedTextureModelBuilder.of(new Variant(baseModel));
    }
    
    /// Starts a builder from a base variant.
    ///
    /// This overload is useful when the caller already has a configured
    /// {@link Variant}, for example after applying rotations or UV locking.
    ///
    /// @param baseModel base variant
    ///
    /// @return model builder
    public static SLConnectedTextureModelBuilder of(Variant baseModel) {
        return new SLConnectedTextureModelBuilder(baseModel, List.of());
    }
    
    /// Adds a target using the default same-block connection rule.
    ///
    /// If no faces are supplied, the target applies to every face.
    ///
    /// @param id      target id
    /// @param texture connected-texture atlas
    /// @param faces   faces using the atlas
    ///
    /// @return updated builder
    public SLConnectedTextureModelBuilder target(String id, Identifier texture, Direction... faces) {
        EnumSet<Direction> resolvedFaces = faces.length == 0 ? EnumSet.allOf(Direction.class) : EnumSet.copyOf(Arrays.asList(faces));
        return this.target(id, texture, SLConnectedTextureRules.sameBlock(), resolvedFaces);
    }
    
    /// Adds a target with the supplied connection rule.
    ///
    /// This overload accepts varargs for convenience. An empty vararg means
    /// the target should apply to all faces.
    ///
    /// @param id      target id
    /// @param texture connected-texture atlas
    /// @param rule    serialized connection rule
    /// @param faces   faces using the atlas
    ///
    /// @return updated builder
    public SLConnectedTextureModelBuilder target(String id, Identifier texture, SLConnectedTextureRule rule, Direction... faces) {
        EnumSet<Direction> resolvedFaces = faces.length == 0 ? EnumSet.allOf(Direction.class) : EnumSet.copyOf(Arrays.asList(faces));
        return this.target(id, texture, rule, resolvedFaces);
    }
    
    /// Adds a target with the supplied connection rule.
    ///
    /// The created target always uses the blob-47 layout and registers the
    /// provided texture as the {@value SLConnectedTextureTarget#DEFAULT_VARIANT}
    /// atlas for the target.
    ///
    /// @param id      target id
    /// @param texture connected-texture atlas
    /// @param rule    serialized connection rule
    /// @param faces   faces using the atlas
    ///
    /// @return updated builder
    public SLConnectedTextureModelBuilder target(String id, Identifier texture, SLConnectedTextureRule rule, EnumSet<Direction> faces) {
        List<SLConnectedTextureTarget> updatedTargets = new ArrayList<>(this.targets);
        updatedTargets.add(new SLConnectedTextureTarget(
                id,
                texture,
                Map.of(SLConnectedTextureTarget.DEFAULT_VARIANT, texture),
                List.of(),
                rule,
                faces,
                SLBlob47ConnectedTextureLayout.INSTANCE,
                SLConnectedTextureMasks.NONE));
        return new SLConnectedTextureModelBuilder(this.baseModel, updatedTargets);
    }
    
    /// Adds a target with the supplied connection rule on all faces.
    ///
    /// This is equivalent to passing every {@link Direction} explicitly.
    ///
    /// @param id      target id
    /// @param texture connected-texture atlas
    /// @param rule    serialized connection rule
    ///
    /// @return updated builder
    public SLConnectedTextureModelBuilder target(String id, Identifier texture, SLConnectedTextureRule rule) {
        return this.target(id, texture, rule, EnumSet.allOf(Direction.class));
    }
    
    /// Adds a target using the default same-block rule on all faces.
    ///
    /// This is the shortest way to attach a standard connected-texture atlas
    /// to a cube-style model.
    ///
    /// @param id      target id
    /// @param texture connected-texture atlas
    ///
    /// @return updated builder
    public SLConnectedTextureModelBuilder target(String id, Identifier texture) {
        return this.target(id, texture, SLConnectedTextureRules.sameBlock(), EnumSet.allOf(Direction.class));
    }
    
    /// Applies a variant mutator to the wrapped base model while preserving all
    /// connected-texture targets accumulated so far.
    ///
    /// @param variantMutator mutator applied to the base variant
    ///
    /// @return updated builder
    @Override
    public SLConnectedTextureModelBuilder with(VariantMutator variantMutator) {
        return new SLConnectedTextureModelBuilder(this.baseModel.with(variantMutator), this.targets);
    }
    
    /// Applies an unbaked mutator to the wrapped base model.
    ///
    /// Connected-texture builders only support a single-variant base model.
    /// If the mutator returns anything else, this method throws an
    /// {@link UnsupportedOperationException}.
    ///
    /// @param variantMutator mutator applied to the base variant
    ///
    /// @return updated builder
    @Override
    public SLConnectedTextureModelBuilder with(UnbakedMutator variantMutator) {
        BlockStateModel.Unbaked mutated = variantMutator.apply(new SingleVariant.Unbaked(this.baseModel));
        if (mutated instanceof SingleVariant.Unbaked(Variant variant)) {
            return new SLConnectedTextureModelBuilder(variant, this.targets);
        }
        throw new UnsupportedOperationException("Connected-texture builders only support single-variant base models");
    }
    
    /// Converts the builder into the serializable unbaked model definition used
    /// by datagen output.
    ///
    /// @return unbaked connected-texture model
    @Override
    public SLConnectedTextureModel toUnbaked() {
        return new SLConnectedTextureModel(this.baseModel, this.targets);
    }
}
