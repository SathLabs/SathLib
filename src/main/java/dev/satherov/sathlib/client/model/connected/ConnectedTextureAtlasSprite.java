package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

///
/// Sprite view for one tile inside a connected-texture atlas sprite.
///
public class ConnectedTextureAtlasSprite extends TextureAtlasSprite {
    
    private final TextureAtlasSprite delegate;
    private final int atlasX;
    private final int atlasY;
    private final int pixelOffsetX;
    private final int pixelOffsetY;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    
    ///
    /// Creates a sprite view for one atlas tile.
    ///
    /// @param delegate source atlas sprite
    /// @param tile     tile coordinates inside the connected-texture sheet
    ///
    protected ConnectedTextureAtlasSprite(final TextureAtlasSprite delegate, final SpriteSheet.Pos tile) {
        
        // The size of a single sprite is the atlas size multiplied with the partition fraction of a tile
        int width = Math.round(delegate.contents().width() * SpriteSheet.PARTITION);
        int height = Math.round(delegate.contents().height() * SpriteSheet.PARTITION);
        
        // Offset of the sprite we want within the connected texture atlas
        final int pixelOffsetX = tile.x() * width;
        final int pixelOffsetY = tile.y() * height;
        
        // Offset of the connected texture atlas from within the full texture atlas
        final int atlasX = delegate.getX() + pixelOffsetX;
        final int atlasY = delegate.getY() + pixelOffsetY;
        
        super(
                delegate.atlasLocation(),
                delegate.contents(),
                ConnectedTextureAtlasSprite.atlasWidth(delegate),
                ConnectedTextureAtlasSprite.atlasHeight(delegate),
                atlasX,
                atlasY,
                0
        );
        
        this.delegate = delegate;
        
        this.pixelOffsetX = pixelOffsetX;
        this.pixelOffsetY = pixelOffsetY;
        
        this.atlasX = atlasX;
        this.atlasY = atlasY;
        
        final float fullWidth = delegate.contents().width();
        final float fullHeight = delegate.contents().height();
        
        this.u0 = delegate.getU(this.pixelOffsetX / fullWidth);
        this.u1 = delegate.getU((this.pixelOffsetX + width) / fullWidth);
        this.v0 = delegate.getV(this.pixelOffsetY / fullHeight);
        this.v1 = delegate.getV((this.pixelOffsetY + height) / fullHeight);
    }
    
    private static int atlasWidth(final TextureAtlasSprite sprite) {
        return Math.max(1, Math.round(sprite.contents().width() / (sprite.getU1() - sprite.getU0())));
    }
    
    private static int atlasHeight(final TextureAtlasSprite sprite) {
        return Math.max(1, Math.round(sprite.contents().height() / (sprite.getV1() - sprite.getV0())));
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
    public float getU(final float offset) {
        return this.u0 + ((this.u1 - this.u0) * offset);
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
    public float getV(final float offset) {
        return this.v0 + ((this.v1 - this.v0) * offset);
    }
    
    @Override
    public int getPixelRGBA(final int frameIndex, final int x, final int y) {
        return this.delegate.getPixelRGBA(frameIndex, this.pixelOffsetX + x, this.pixelOffsetY + y);
    }
}
