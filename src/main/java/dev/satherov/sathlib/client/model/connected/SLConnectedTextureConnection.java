package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.resources.model.sprite.Material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SLConnectedTextureConnection(Material texture, SLConnectedTextureRule rule) {
    
    public static final Codec<SLConnectedTextureConnection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.fieldOf("texture").forGetter(SLConnectedTextureConnection::texture),
            SLConnectedTextureRule.CODEC.fieldOf("rule").forGetter(SLConnectedTextureConnection::rule)
    ).apply(instance, SLConnectedTextureConnection::new));
}
