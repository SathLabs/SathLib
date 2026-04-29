package dev.satherov.sathlib.client.model.connected;

///
/// Connected texture atlas lookup table that maps face masks to atlas tile positions.
///
public final class SpriteSheet {
    
    ///
    /// Number of horizontal partitions in the connected texture atlas.
    ///
    public static final int SIZE = 8;
    private static final int WIDTH = 8;
    private static final int HEIGHT = 6;
    ///
    /// UV span of a single atlas tile relative to the full sprite.
    ///
    public static final float PARTITION = 1.0F / SpriteSheet.SIZE;
    
    private static final int[][] SPRITES = SpriteSheet.createSprites();
    private static final SpriteSheet.Pos[] CACHE = SpriteSheet.createCache();
    
    private SpriteSheet() { }
    
    private static int[][] createSprites() {
        final int[][] sprites = new int[SpriteSheet.HEIGHT][SpriteSheet.WIDTH];
        
        sprites[0][0] = SpriteMasks.NONE;
        
        sprites[0][1] = SpriteMasks.E;
        sprites[0][2] = SpriteMasks.W | SpriteMasks.E;
        sprites[0][3] = SpriteMasks.W;
        sprites[0][4] = SpriteMasks.S | SpriteMasks.E;
        sprites[0][5] = SpriteMasks.S | SpriteMasks.W;
        sprites[0][6] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.E;
        sprites[0][7] = SpriteMasks.S | SpriteMasks.W | SpriteMasks.E;
        
        sprites[1][0] = SpriteMasks.S;
        sprites[1][1] = SpriteMasks.S | SpriteMasks.E | SpriteMasks.SE;
        sprites[1][2] = SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.SW | SpriteMasks.SE;
        sprites[1][3] = SpriteMasks.S | SpriteMasks.W | SpriteMasks.SW;
        sprites[1][4] = SpriteMasks.N | SpriteMasks.E;
        sprites[1][5] = SpriteMasks.N | SpriteMasks.W;
        sprites[1][6] = SpriteMasks.N | SpriteMasks.W | SpriteMasks.E;
        sprites[1][7] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W;
        
        sprites[2][0] = SpriteMasks.N | SpriteMasks.S;
        sprites[2][1] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.E | SpriteMasks.NE | SpriteMasks.SE;
        sprites[2][2] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.NE | SpriteMasks.SW | SpriteMasks.SE;
        sprites[2][3] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.NW | SpriteMasks.SW;
        sprites[2][4] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.E | SpriteMasks.NE;
        sprites[2][5] = SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.SE;
        sprites[2][6] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.E | SpriteMasks.SE;
        sprites[2][7] = SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.SW;
        
        sprites[3][0] = SpriteMasks.N;
        sprites[3][1] = SpriteMasks.N | SpriteMasks.E | SpriteMasks.NE;
        sprites[3][2] = SpriteMasks.N | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.NE;
        sprites[3][3] = SpriteMasks.N | SpriteMasks.W | SpriteMasks.NW;
        sprites[3][4] = SpriteMasks.N | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW;
        sprites[3][5] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.SW;
        sprites[3][6] = SpriteMasks.N | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NE;
        sprites[3][7] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.NW;
        
        sprites[4][0] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NE | SpriteMasks.SW;
        sprites[4][1] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E;
        sprites[4][2] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NE | SpriteMasks.SE;
        sprites[4][3] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.SW | SpriteMasks.SE;
        sprites[4][4] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.SE;
        sprites[4][5] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.SW;
        sprites[4][6] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.NE | SpriteMasks.SW;
        sprites[4][7] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.NE | SpriteMasks.SE;
        
        sprites[5][0] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.SE;
        //sprites[5][1] = Nonexistent in the atlas
        sprites[5][2] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.NE;
        sprites[5][3] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.SW;
        sprites[5][4] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NE;
        sprites[5][5] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW;
        sprites[5][6] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NW | SpriteMasks.SW | SpriteMasks.SE;
        sprites[5][7] = SpriteMasks.N | SpriteMasks.S | SpriteMasks.W | SpriteMasks.E | SpriteMasks.NE | SpriteMasks.SW | SpriteMasks.SE;
        
        return sprites;
    }
    
    private static SpriteSheet.Pos[] createCache() {
        final SpriteSheet.Pos[] cache = new SpriteSheet.Pos[256];
        
        cache[SpriteMasks.NONE] = SpriteSheet.Pos.ORIGIN;
        
        for (int y = 0; y < SpriteSheet.SPRITES.length; y++) {
            for (int x = 0; x < SpriteSheet.SPRITES[y].length; x++) {
                if (x == 0 && y == 0) continue;
                final int mask = SpriteSheet.SPRITES[y][x];
                if (mask != SpriteMasks.NONE) cache[mask] = new SpriteSheet.Pos(x, y);
            }
        }
        
        return cache;
    }
    
    ///
    /// Resolves the atlas position for a sprite mask.
    ///
    /// @param mask sprite connection mask
    ///
    /// @return atlas tile position for the mask
    ///
    public static Pos resolve(int mask) {
        final SpriteSheet.Pos pos = SpriteSheet.CACHE[mask & 0xFF];
        return pos != null ? pos : Pos.ORIGIN;
    }
    
    ///
    /// Immutable atlas tile position.
    ///
    /// @param x x coordinate in atlas tile units
    /// @param y y coordinate in atlas tile units
    ///
    public record Pos(int x, int y) {
        ///
        /// Origin tile of the connected texture atlas.
        ///
        public static final Pos ORIGIN = new Pos(0, 0);
    }
}
