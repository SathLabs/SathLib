package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.resources.model.sprite.Material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

///
/// One connected-texture layer binding.
///
/// @param texture texture atlas material to remap
/// @param rule    connection rule that selects this layer
///
public record ConnectedTextureLayer(Material texture, ConnectionPredicate rule) {
    
    public static final Codec<ConnectedTextureLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.fieldOf("texture").forGetter(ConnectedTextureLayer::texture),
            ConnectionRules.CODEC.fieldOf("rule").forGetter(ConnectedTextureLayer::rule)
    ).apply(instance, ConnectedTextureLayer::new));
}
