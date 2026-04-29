package dev.satherov.sathlib.client.model.connected;

@SuppressWarnings("PointlessBitwiseExpression")
public final class SpriteMasks {
    
    public static final int NONE = 0;
    
    public static final int N = 1 << 0;
    public static final int NE = 1 << 1;
    public static final int E = 1 << 2;
    public static final int SE = 1 << 3;
    public static final int S = 1 << 4;
    public static final int SW = 1 << 5;
    public static final int W = 1 << 6;
    public static final int NW = 1 << 7;
    
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
    
    public static boolean has(int mask, int direction) {
        return (mask & direction) != 0;
    }
}