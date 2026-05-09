package dev.satherov.sathlib.client.model.connected;

import lombok.experimental.UtilityClass;

import net.neoforged.neoforge.client.model.quad.MutableQuad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;

@UtilityClass
public class ConnectedTextureUvResolver {
    
    ///
    /// Remaps a baked quad to the supplied connected texture tile.
    ///
    /// @param quad quad to remap
    /// @param tile target atlas tile
    ///
    /// @return remapped quad
    ///
    public static BakedQuad remap(final BakedQuad quad, final SpriteSheet.Pos tile) {
        final MutableQuad mutableQuad = new MutableQuad().setFrom(quad);
        final TextureAtlasSprite sprite = quad.materialInfo().sprite();
        final TileUv tileUv = ConnectedTextureUvResolver.tileUv(sprite, tile);
        
        for (int i = 0; i < 4; i++) {
            final float localU = ConnectedTextureUvResolver.normalize(mutableQuad.u(i), sprite.getU0(), sprite.getU1());
            final float localV = ConnectedTextureUvResolver.normalize(mutableQuad.v(i), sprite.getV0(), sprite.getV1());
            final float spriteU = tileUv.spriteU(localU);
            final float spriteV = tileUv.spriteV(localV);
            
            mutableQuad.setUv(i, sprite.getU(spriteU), sprite.getV(spriteV));
        }
        
        return mutableQuad.toBakedQuad();
    }
    
    ///
    /// Remaps a baked material to the supplied connected texture tile.
    ///
    /// @param material material to remap
    /// @param tile     target atlas tile
    ///
    /// @return remapped material
    ///
    public static Material.Baked remap(final Material.Baked material, final SpriteSheet.Pos tile) {
        return new Material.Baked(ConnectedTextureUvResolver.remap(material.sprite(), tile), material.forceTranslucent());
    }
    
    ///
    /// Returns a texture sprite from the connected texture atlas at the specified tile position.
    ///
    /// @param sprite sprite to remap
    /// @param tile   target atlas tile
    ///
    /// @return remapped sprite
    ///
    public static TextureAtlasSprite remap(final TextureAtlasSprite sprite, final SpriteSheet.Pos tile) {
        return new ConnectedTextureAtlasSprite(sprite, tile);
    }
    
    ///
    /// Maps a `value` in the range `[min, max]` to the range `[0, 1]`.
    ///
    /// @param value value to normalize
    /// @param min   minimum value of the range
    /// @param max   maximum value of the range
    ///
    /// @return normalized value
    ///
    private static float normalize(final float value, final float min, final float max) {
        if (max == min) return 0.0F;
        return (value - min) / (max - min);
    }
    
    ///
    /// Computes the UV coordinates of a single texture tile in the connected texture atlas.
    ///
    /// @param sprite sprite to compute UV coordinates for
    /// @param tile   atlas tile to compute UV coordinates for
    ///
    /// @return UV coordinates of the tile
    ///
    private static TileUv tileUv(final TextureAtlasSprite sprite, final SpriteSheet.Pos tile) {
        final float fullWidth = sprite.contents().width();
        final float fullHeight = sprite.contents().height();
        final float tileWidth = fullWidth * SpriteSheet.PARTITION;
        final float tileHeight = fullHeight * SpriteSheet.PARTITION;
        final float pixelOffsetX = tile.x() * tileWidth;
        final float pixelOffsetY = tile.y() * tileHeight;
        
        return new TileUv(fullWidth, fullHeight, tileWidth, tileHeight, pixelOffsetX, pixelOffsetY);
    }
    
    ///
    /// Holder record for the UV coordinates of a single texture tile in the connected texture atlas.
    ///
    /// @param fullWidth    full width of the connected texture atlas
    /// @param fullHeight   full height of the connected texture atlas
    /// @param tileWidth    width of a single tile in the connected texture atlas
    /// @param tileHeight   height of a single tile in the connected texture atlas
    /// @param pixelOffsetX offset of the tile in the connected texture atlas
    /// @param pixelOffsetY offset of the tile in the connected texture atlas
    ///
    private record TileUv(
            float fullWidth,
            float fullHeight,
            float tileWidth,
            float tileHeight,
            float pixelOffsetX,
            float pixelOffsetY
    ) {
        private float spriteU(final float localU) {
            return (this.pixelOffsetX + (localU * this.tileWidth)) / this.fullWidth;
        }
        
        private float spriteV(final float localV) {
            return (this.pixelOffsetY + (localV * this.tileHeight)) / this.fullHeight;
        }
    }
}
