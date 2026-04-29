package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.resources.model.sprite.Material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

///
/// Texture binding used by a connected texture model.
///
/// @param texture connected texture atlas material
/// @param rule    rule deciding when the texture should connect
///
public record SLConnectedTextureConnection(Material texture, SLConnectedTextureRule rule) {
    
    ///
    /// Codec used to serialize connected texture bindings.
    ///
    public static final Codec<SLConnectedTextureConnection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.fieldOf("texture").forGetter(SLConnectedTextureConnection::texture),
            SLConnectedTextureRule.CODEC.fieldOf("rule").forGetter(SLConnectedTextureConnection::rule)
    ).apply(instance, SLConnectedTextureConnection::new));
}
