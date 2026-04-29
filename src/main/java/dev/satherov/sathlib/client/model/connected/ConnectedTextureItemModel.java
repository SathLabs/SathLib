package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@NothingNull
public final class ConnectedTextureItemModel {
    
    private static final double MODEL_UV_SIZE = 16.0D * SpriteSheet.PARTITION;
    
    private ConnectedTextureItemModel() { }
    
    public static Identifier create(
            final BlockModelGenerators generators,
            final Block block,
            final Identifier parent,
            final Material texture
    ) {
        return ConnectedTextureItemModel.create(
                generators,
                ModelLocationUtils.getModelLocation(block, "_connected_item"),
                parent,
                texture
        );
    }
    
    public static Identifier create(
            final BlockModelGenerators generators,
            final Identifier modelId,
            final Identifier parent,
            final Material texture
    ) {
        generators.modelOutput.accept(modelId, () -> ConnectedTextureItemModel.json(parent, texture.sprite().toString(), SpriteSheet.Pos.ORIGIN));
        return modelId;
    }
    
    private static JsonObject json(final Identifier parent, final String texture, final SpriteSheet.Pos tile) {
        final JsonObject json = new JsonObject();
        json.addProperty("parent", parent.toString());
        
        final JsonObject textures = new JsonObject();
        textures.addProperty("all", texture);
        textures.addProperty("particle", texture);
        json.add("textures", textures);
        
        final JsonArray elements = new JsonArray();
        elements.add(ConnectedTextureItemModel.element(tile));
        json.add("elements", elements);
        return json;
    }
    
    private static JsonObject element(final SpriteSheet.Pos tile) {
        final JsonObject element = new JsonObject();
        element.add("from", ConnectedTextureItemModel.vector(0.0D, 0.0D, 0.0D));
        element.add("to", ConnectedTextureItemModel.vector(16.0D, 16.0D, 16.0D));
        
        final JsonObject faces = new JsonObject();
        faces.add("down", ConnectedTextureItemModel.face(tile));
        faces.add("up", ConnectedTextureItemModel.face(tile));
        faces.add("north", ConnectedTextureItemModel.face(tile));
        faces.add("south", ConnectedTextureItemModel.face(tile));
        faces.add("west", ConnectedTextureItemModel.face(tile));
        faces.add("east", ConnectedTextureItemModel.face(tile));
        
        element.add("faces", faces);
        return element;
    }
    
    private static JsonObject face(final SpriteSheet.Pos tile) {
        final double minU = tile.x() * ConnectedTextureItemModel.MODEL_UV_SIZE;
        final double minV = tile.y() * ConnectedTextureItemModel.MODEL_UV_SIZE;
        final double maxU = minU + ConnectedTextureItemModel.MODEL_UV_SIZE;
        final double maxV = minV + ConnectedTextureItemModel.MODEL_UV_SIZE;
        
        final JsonObject face = new JsonObject();
        face.add("uv", ConnectedTextureItemModel.vector(minU, minV, maxU, maxV));
        face.addProperty("texture", "#all");
        return face;
    }
    
    private static JsonArray vector(final double... values) {
        final JsonArray array = new JsonArray();
        for (final double value : values) {
            array.add(value);
        }
        
        return array;
    }
}
