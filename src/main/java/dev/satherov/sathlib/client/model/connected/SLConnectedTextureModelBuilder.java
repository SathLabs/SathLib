package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

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
    
    public static SLConnectedTextureModelBuilder connected(final Variant variant) {
        return new SLConnectedTextureModelBuilder(new SingleVariant.Unbaked(variant), List.of());
    }
    
    public static SLConnectedTextureModelBuilder connected(final Identifier model) {
        return SLConnectedTextureModelBuilder.connected(new Variant(model));
    }
    
    public static SLConnectedTextureModelBuilder connected(final BlockStateModel.Unbaked model) {
        return new SLConnectedTextureModelBuilder(model, List.of());
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
        return SLConnectedTextureModelBuilder.createItemModel(generators, block, baseModel, new Material(texture));
    }
    
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
    
    public SLConnectedTextureModelBuilder connect(final Identifier texture, final SLConnectedTextureRule rule) {
        return this.connect(new Material(texture), rule);
    }

    public Identifier createItemModel(final BlockModelGenerators generators, final Block block) {
        return ConnectedTextureItemModel.create(
                generators,
                ModelLocationUtils.getModelLocation(block, "_connected_item"),
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
        if (this.model instanceof SingleVariant.Unbaked(Variant variant)) {
            return variant.modelLocation();
        }
        
        throw new IllegalStateException("Connected texture item model generation requires a single-variant base model");
    }
    
    private Material resolveItemTexture() {
        if (this.connections.isEmpty()) {
            throw new IllegalStateException("Connected texture item model generation requires at least one connected texture");
        }
        
        return this.connections.getFirst().texture();
    }
}
