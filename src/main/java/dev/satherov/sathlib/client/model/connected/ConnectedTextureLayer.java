package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.resources.model.sprite.Material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ConnectedTextureLayer(Material texture, ConnectionPredicate rule) {
    
    public static final Codec<ConnectedTextureLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.fieldOf("texture").forGetter(ConnectedTextureLayer::texture),
            ConnectionRules.CODEC.fieldOf("rule").forGetter(ConnectedTextureLayer::rule)
    ).apply(instance, ConnectedTextureLayer::new));
}
