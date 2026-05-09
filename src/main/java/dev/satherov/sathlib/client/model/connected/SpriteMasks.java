package dev.satherov.sathlib.client.model.connected;

///
/// Bit-mask constants describing connected neighbors around a sprite tile.
///
public final class SpriteMasks {
    
    ///
    /// Empty connection mask.
    ///
    public static final int NONE = 0;
    
    ///
    /// North connection bit.
    ///
    public static final int N = 1;
    ///
    /// North-east connection bit.
    ///
    public static final int NE = 1 << 1;
    ///
    /// East connection bit.
    ///
    public static final int E = 1 << 2;
    ///
    /// South-east connection bit.
    ///
    public static final int SE = 1 << 3;
    ///
    /// South connection bit.
    ///
    public static final int S = 1 << 4;
    ///
    /// South-west connection bit.
    ///
    public static final int SW = 1 << 5;
    ///
    /// West connection bit.
    ///
    public static final int W = 1 << 6;
    ///
    /// North-west connection bit.
    ///
    public static final int NW = 1 << 7;
    
    private SpriteMasks() { }
    
    ///
    /// Builds a sprite mask from individual neighbor flags.
    ///
    /// @param north     whether the north connection is present
    /// @param northEast whether the north-east connection is present
    /// @param east      whether the east connection is present
    /// @param southEast whether the south-east connection is present
    /// @param south     whether the south connection is present
    /// @param southWest whether the south-west connection is present
    /// @param west      whether the west connection is present
    /// @param northWest whether the north-west connection is present
    ///
    /// @return assembled sprite mask
    ///
    public static int of(
            boolean north,
            boolean northEast,
            boolean east,
            boolean southEast,
            boolean south,
            boolean southWest,
            boolean west,
            boolean northWest
    ) {
        
        int mask = 0;
        if (north) mask |= SpriteMasks.N;
        if (northEast) mask |= SpriteMasks.NE;
        if (east) mask |= SpriteMasks.E;
        if (southEast) mask |= SpriteMasks.SE;
        if (south) mask |= SpriteMasks.S;
        if (southWest) mask |= SpriteMasks.SW;
        if (west) mask |= SpriteMasks.W;
        if (northWest) mask |= SpriteMasks.NW;
        
        return mask;
    }
}
