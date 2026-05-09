package dev.satherov.sathlib.data.model;

import dev.satherov.sathlib.client.model.connected.ConnectedTextureBlockModel;
import dev.satherov.sathlib.client.model.connected.ConnectedTextureLayer;
import dev.satherov.sathlib.client.model.connected.ConnectionPredicate;
import dev.satherov.sathlib.client.model.connected.SpriteSheet;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@NothingNull
public class SLConnectedTextureModelBuilder extends CustomBlockStateModelBuilder {
    
    private final BlockStateModel.Unbaked model;
    private final List<ConnectedTextureLayer> layers;
    
    private SLConnectedTextureModelBuilder(BlockStateModel.Unbaked model, List<ConnectedTextureLayer> layers) {
        this.model = model;
        this.layers = new ArrayList<>(layers);
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
    /// Adds a connected texture binding to this builder.
    ///
    /// @param texture texture atlas material to bind
    /// @param rule    connection rule applied to the texture
    ///
    /// @return updated builder
    ///
    public SLConnectedTextureModelBuilder connect(final Material texture, final ConnectionPredicate rule) {
        if (this.layers.stream().anyMatch(layer -> layer.texture().sprite().equals(texture.sprite()))) {
            throw new IllegalStateException("Duplicate connected texture sprite binding for " + texture.sprite());
        }
        
        this.layers.add(new ConnectedTextureLayer(texture, rule));
        return this;
    }
    
    ///
    /// Adds a connected texture binding to this builder.
    ///
    /// @param texture texture atlas identifier to bind
    /// @param rule    connection rule applied to the texture
    ///
    /// @return updated builder
    ///
    public SLConnectedTextureModelBuilder connect(final Identifier texture, final ConnectionPredicate rule) {
        return this.connect(new Material(texture), rule);
    }
    
    @Override
    public SLConnectedTextureModelBuilder with(VariantMutator mutator) {
        if (this.model instanceof SingleVariant.Unbaked(Variant variant)) {
            return new SLConnectedTextureModelBuilder(new SingleVariant.Unbaked(variant.with(mutator)), this.layers);
        }
        return this;
    }
    
    @Override
    public SLConnectedTextureModelBuilder with(UnbakedMutator mutator) {
        return new SLConnectedTextureModelBuilder(mutator.apply(this.model), this.layers);
    }
    
    @Override
    public ConnectedTextureBlockModel.Unbaked toUnbaked() {
        return new ConnectedTextureBlockModel.Unbaked(this.model, this.layers);
    }
    
    ///
    /// Creates an item model for a block with a connected texture atlas
    ///
    /// @param generators block model generators
    /// @param block      block to generate an item-model for
    ///
    /// @return generated item model identifier
    ///
    public Identifier createItem(final BlockModelGenerators generators, final Supplier<? extends Block> block) {
        final Identifier identifier = ModelLocationUtils.getModelLocation(block.get(), "_connected");
        ExtendedModelTemplateBuilder.builder()
                .parent(this.resolveItemModelBase())
                .requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element
                        .from(0.0F, 0.0F, 0.0F)
                        .to(16.0F, 16.0F, 16.0F)
                        .allFaces((_, face) -> face.texture(TextureSlot.ALL).uvs(
                                0, 0,
                                SpriteSheet.CTM_UV_SIZE, SpriteSheet.CTM_UV_SIZE
                        ))
                )
                .build()
                .create(identifier, TextureMapping.cube(this.resolveItemTexture()).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE), generators.modelOutput);
        return identifier;
    }
    
    private Identifier resolveItemModelBase() {
        if (this.model instanceof SingleVariant.Unbaked(Variant variant)) return variant.modelLocation();
        throw new IllegalStateException("Connected texture item model generation requires a single-variant base model");
    }
    
    private Material resolveItemTexture() {
        if (this.layers.isEmpty()) throw new IllegalStateException("Connected texture item model generation requires at least one connected texture");
        return this.layers.getFirst().texture();
    }
}
