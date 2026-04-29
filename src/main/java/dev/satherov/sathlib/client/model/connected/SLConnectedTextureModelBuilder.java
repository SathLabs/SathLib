package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

///
/// Builder for connected texture models that use explicit texture-to-rule bindings.
///
@NothingNull
public final class SLConnectedTextureModelBuilder extends CustomBlockStateModelBuilder {
    
    private final BlockStateModel.Unbaked model;
    private final List<SLConnectedTextureConnection> connections;
    
    private SLConnectedTextureModelBuilder(
            final BlockStateModel.Unbaked model,
            final List<SLConnectedTextureConnection> connections
    ) {
        this.model = model;
        this.connections = List.copyOf(connections);
    }
    
    ///
    /// Starts a connected texture builder from a variant.
    ///
    /// @param variant base variant to wrap
    ///
    /// @return connected texture model builder
    ///
    public static SLConnectedTextureModelBuilder connected(final Variant variant) {
        return new SLConnectedTextureModelBuilder(new SingleVariant.Unbaked(variant), List.of());
    }
    
    ///
    /// Starts a connected texture builder from a model identifier.
    ///
    /// @param model base model identifier
    ///
    /// @return connected texture model builder
    ///
    public static SLConnectedTextureModelBuilder connected(final Identifier model) {
        return SLConnectedTextureModelBuilder.connected(new Variant(model));
    }
    
    ///
    /// Starts a connected texture builder from an unbaked blockstate model.
    ///
    /// @param model base unbaked model
    ///
    /// @return connected texture model builder
    ///
    public static SLConnectedTextureModelBuilder connected(final BlockStateModel.Unbaked model) {
        return new SLConnectedTextureModelBuilder(model, List.of());
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
        return SLConnectedTextureModelBuilder.createItemModel(generators, block, baseModel, new Material(texture));
    }
    
    ///
    /// Adds a connected texture binding to this builder.
    ///
    /// @param texture texture atlas material to bind
    /// @param rule    connection rule applied to the texture
    ///
    /// @return updated builder
    ///
    public SLConnectedTextureModelBuilder connect(final Material texture, final SLConnectedTextureRule rule) {
        final ArrayList<SLConnectedTextureConnection> updatedConnections = new ArrayList<>(this.connections);
        
        for (final SLConnectedTextureConnection existing : updatedConnections) {
            if (existing.texture().sprite().equals(texture.sprite())) {
                throw new IllegalStateException("Duplicate connected texture sprite binding for " + texture.sprite());
            }
        }
        
        updatedConnections.add(new SLConnectedTextureConnection(texture, rule));
        return new SLConnectedTextureModelBuilder(this.model, updatedConnections);
    }
    
    ///
    /// Adds a connected texture binding to this builder.
    ///
    /// @param texture texture atlas identifier to bind
    /// @param rule    connection rule applied to the texture
    ///
    /// @return updated builder
    ///
    public SLConnectedTextureModelBuilder connect(final Identifier texture, final SLConnectedTextureRule rule) {
        return this.connect(new Material(texture), rule);
    }
    
    ///
    /// Creates the connected-texture item model for this builder's first bound texture.
    ///
    /// @param generators data generators receiving the model
    /// @param block      block to generate the item model for
    ///
    /// @return generated item model identifier
    ///
    public Identifier createItemModel(final BlockModelGenerators generators, final Block block) {
        return ConnectedTextureItemModel.create(
                generators,
                block,
                this.resolveItemModelBase(),
                this.resolveItemTexture()
        );
    }
    
    @Override
    public CustomBlockStateModelBuilder with(final VariantMutator variantMutator) {
        if (this.model instanceof SingleVariant.Unbaked(Variant variant)) {
            return new SLConnectedTextureModelBuilder(new SingleVariant.Unbaked(variant.with(variantMutator)), this.connections);
        }
        
        return this;
    }
    
    @Override
    public CustomBlockStateModelBuilder with(final UnbakedMutator variantMutator) {
        return new SLConnectedTextureModelBuilder(variantMutator.apply(this.model), this.connections);
    }
    
    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return new ConnectedTextureBlockModel.Unbaked(this.model, null, this.connections);
    }
    
    private Identifier resolveItemModelBase() {
        if (this.model instanceof SingleVariant.Unbaked(Variant variant)) return variant.modelLocation();
        throw new IllegalStateException("Connected texture item model generation requires a single-variant base model");
    }
    
    private Material resolveItemTexture() {
        if (this.connections.isEmpty()) throw new IllegalStateException("Connected texture item model generation requires at least one connected texture");
        return this.connections.getFirst().texture();
    }
}
