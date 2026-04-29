package dev.satherov.sathlib.client.model.connected;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

///
/// Small utility class for generating item models from a block with a connected texture atlas
///
@UtilityClass
@NothingNull
public final class ConnectedTextureItemModel {
    
    /// UV size of one tile from the texture atlas,
    /// scaled to match the entire atlas being mapped to 16x16
    private static final float UV_SIZE = 16.0F * SpriteSheet.PARTITION;
    
    ///
    /// Creates an item model for a block with a connected texture atlas
    ///
    /// @param generators block model generators
    /// @param block      block to generate an item-model for
    /// @param parent     parent model identifier
    /// @param texture    texture atlas material
    ///
    /// @return generated item model identifier
    ///
    public static Identifier create(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier parent,
            final Material texture
    ) {
        final Identifier identifier = ModelLocationUtils.getModelLocation(block, "_connected");
        ConnectedTextureItemModel.template(parent).create(identifier, TextureMapping.cube(texture).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE), generators.modelOutput);
        return identifier;
    }
    
    ///
    /// Creates a model template for the item model
    ///
    /// @param parent parent model identifier
    ///
    /// @return model template
    ///
    private static ExtendedModelTemplate template(final Identifier parent) {
        return ExtendedModelTemplateBuilder.builder()
                .parent(parent)
                .requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element
                        .from(0.0F, 0.0F, 0.0F)
                        .to(16.0F, 16.0F, 16.0F)
                        .allFaces((_, face) -> face.texture(TextureSlot.ALL).uvs(
                                0, 0,
                                ConnectedTextureItemModel.UV_SIZE, ConnectedTextureItemModel.UV_SIZE
                        ))
                )
                .build();
    }
}
