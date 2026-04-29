package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@NothingNull
final class ConnectedTextureSubSprite extends TextureAtlasSprite {
    
    private final TextureAtlasSprite delegate;
    private final int atlasX;
    private final int atlasY;
    private final int pixelOffsetX;
    private final int pixelOffsetY;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    
    private ConnectedTextureSubSprite(final TextureAtlasSprite delegate, final SpriteSheet.Pos tile) {
        super(
                delegate.atlasLocation(),
                delegate.contents(),
                ConnectedTextureSubSprite.atlasWidth(delegate),
                ConnectedTextureSubSprite.atlasHeight(delegate),
                ConnectedTextureSubSprite.atlasX(delegate, tile),
                ConnectedTextureSubSprite.atlasY(delegate, tile),
                0
        );
        
        this.delegate = delegate;
        int width = Math.round((float) (delegate.contents().width() * SpriteSheet.PARTITION));
        int height = Math.round((float) (delegate.contents().height() * SpriteSheet.PARTITION));
        this.pixelOffsetX = tile.x() * width;
        this.pixelOffsetY = tile.y() * height;
        this.atlasX = delegate.getX() + this.pixelOffsetX;
        this.atlasY = delegate.getY() + this.pixelOffsetY;
        
        final float fullWidth = delegate.contents().width();
        final float fullHeight = delegate.contents().height();
        
        this.u0 = delegate.getU(this.pixelOffsetX / fullWidth);
        this.u1 = delegate.getU((this.pixelOffsetX + width) / fullWidth);
        this.v0 = delegate.getV(this.pixelOffsetY / fullHeight);
        this.v1 = delegate.getV((this.pixelOffsetY + height) / fullHeight);
    }
    
    public static TextureAtlasSprite of(final TextureAtlasSprite delegate, final SpriteSheet.Pos tile) {
        return new ConnectedTextureSubSprite(delegate, tile);
    }
    
    @Override
    public int getX() {
        return this.atlasX;
    }
    
    @Override
    public int getY() {
        return this.atlasY;
    }
    
    @Override
    public float getU0() {
        return this.u0;
    }
    
    @Override
    public float getU1() {
        return this.u1;
    }
    
    @Override
    public float getV0() {
        return this.v0;
    }
    
    @Override
    public float getV1() {
        return this.v1;
    }
    
    @Override
    public float getU(final float offset) {
        return this.u0 + ((this.u1 - this.u0) * offset);
    }
    
    @Override
    public float getV(final float offset) {
        return this.v0 + ((this.v1 - this.v0) * offset);
    }
    
    @Override
    public int getPixelRGBA(final int frameIndex, final int x, final int y) {
        return this.delegate.getPixelRGBA(frameIndex, this.pixelOffsetX + x, this.pixelOffsetY + y);
    }
    
    private static int atlasX(final TextureAtlasSprite sprite, final SpriteSheet.Pos tile) {
        return sprite.getX() + (tile.x() * ConnectedTextureSubSprite.tileWidth(sprite));
    }
    
    private static int atlasY(final TextureAtlasSprite sprite, final SpriteSheet.Pos tile) {
        return sprite.getY() + (tile.y() * ConnectedTextureSubSprite.tileHeight(sprite));
    }
    
    private static int atlasWidth(final TextureAtlasSprite sprite) {
        return Math.max(1, Math.round(sprite.contents().width() / (sprite.getU1() - sprite.getU0())));
    }
    
    private static int atlasHeight(final TextureAtlasSprite sprite) {
        return Math.max(1, Math.round(sprite.contents().height() / (sprite.getV1() - sprite.getV0())));
    }
    
    private static int tileWidth(final TextureAtlasSprite sprite) {
        return Math.round((float) (sprite.contents().width() * SpriteSheet.PARTITION));
    }
    
    private static int tileHeight(final TextureAtlasSprite sprite) {
        return Math.round((float) (sprite.contents().height() * SpriteSheet.PARTITION));
    }
}
