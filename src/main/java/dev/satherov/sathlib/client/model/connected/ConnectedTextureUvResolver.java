package dev.satherov.sathlib.client.model.connected;

import net.neoforged.neoforge.client.model.quad.MutableQuad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;

public final class ConnectedTextureUvResolver {
    
    private ConnectedTextureUvResolver() { }
    
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

    public static Material.Baked remap(final Material.Baked material, final SpriteSheet.Pos tile) {
        return new Material.Baked(ConnectedTextureUvResolver.remap(material.sprite(), tile), material.forceTranslucent());
    }

    public static TextureAtlasSprite remap(final TextureAtlasSprite sprite, final SpriteSheet.Pos tile) {
        return ConnectedTextureSubSprite.of(sprite, tile);
    }
    
    private static float normalize(final float value, final float min, final float max) {
        if (max == min) return 0.0F;
        return (value - min) / (max - min);
    }

    private static TileUv tileUv(final TextureAtlasSprite sprite, final SpriteSheet.Pos tile) {
        final float fullWidth = sprite.contents().width();
        final float fullHeight = sprite.contents().height();
        final float tileWidth = (float) (fullWidth * SpriteSheet.PARTITION);
        final float tileHeight = (float) (fullHeight * SpriteSheet.PARTITION);
        final float pixelOffsetX = tile.x() * tileWidth;
        final float pixelOffsetY = tile.y() * tileHeight;

        return new TileUv(fullWidth, fullHeight, tileWidth, tileHeight, pixelOffsetX, pixelOffsetY);
    }

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
