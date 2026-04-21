package dev.satherov.sathlib.client.render.model;

import dev.satherov.sathlib.SathLib;

import net.minecraft.resources.Identifier;

import java.util.Arrays;

import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.BOTTOM;
import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.BOTTOM_LEFT;
import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.BOTTOM_RIGHT;
import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.LEFT;
import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.RIGHT;
import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.TOP;
import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.TOP_LEFT;
import static dev.satherov.sathlib.client.render.model.SLConnectedTextureDirection.TOP_RIGHT;

/// Standard 47-case blob layout for an 8x6 texture atlas.
///
/// Each entry maps one normalized 8-neighbor mask to one tile in the atlas.
/// The atlas is addressed in grid coordinates, not UV coordinates, so the table
/// stays readable.
public enum SLBlob47ConnectedTextureLayout implements SLConnectedTextureLayout {
    /// Singleton layout instance.
    INSTANCE;
    
    /// Serialized layout identifier.
    public static final Identifier ID = SathLib.id("blob47_8x6");
    
    private static final int GRID_WIDTH = 8;
    private static final int GRID_HEIGHT = 6;
    
    private static final float REGION_WIDTH = 1.0F / SLBlob47ConnectedTextureLayout.GRID_WIDTH;
    private static final float REGION_HEIGHT = 1.0F / SLBlob47ConnectedTextureLayout.GRID_HEIGHT;
    
    private static final SLConnectedTextureRegion FALLBACK_REGION = SLBlob47ConnectedTextureLayout.region(0, 0);
    private static final SLConnectedTextureRegion[] LOOKUP = SLBlob47ConnectedTextureLayout.createLookup();
    
    @Override
    public Identifier id() {
        return SLBlob47ConnectedTextureLayout.ID;
    }
    
    @Override
    public SLConnectedTextureRegion regionForMask(@SLConnectedTextureMask int mask) {
        int normalizedMask = SLConnectedTextureMasks.normalize(mask);
        return SLBlob47ConnectedTextureLayout.LOOKUP[normalizedMask];
    }
    
    private static SLConnectedTextureRegion[] createLookup() {
        SLConnectedTextureRegion[] lookup = new SLConnectedTextureRegion[256];
        Arrays.fill(lookup, SLBlob47ConnectedTextureLayout.FALLBACK_REGION);
        
        SLBlob47ConnectedTextureLayout.registerFullySurroundedCases(lookup);
        SLBlob47ConnectedTextureLayout.registerThreeSideCases(lookup);
        SLBlob47ConnectedTextureLayout.registerTwoSideCases(lookup);
        SLBlob47ConnectedTextureLayout.registerStraightCases(lookup);
        SLBlob47ConnectedTextureLayout.registerCornerAndEndCases(lookup);
        SLBlob47ConnectedTextureLayout.registerSingleNeighborCases(lookup);
        SLBlob47ConnectedTextureLayout.registerIsolatedCase(lookup);
        
        return lookup;
    }
    
    /// Center/full cases with many surrounding neighbors.
    private static void registerFullySurroundedCases(SLConnectedTextureRegion[] lookup) {
        SLBlob47ConnectedTextureLayout.put(lookup, 2, 2,
                RIGHT, TOP_RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 7, 5,
                RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 6, 5,
                RIGHT, TOP_RIGHT, TOP, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 3, 4,
                RIGHT, TOP, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 6, 4,
                RIGHT, TOP_RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 0, 4,
                RIGHT, TOP_RIGHT, TOP, LEFT, BOTTOM_LEFT, BOTTOM);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 3, 5,
                RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 5, 4,
                RIGHT, TOP, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 7, 4,
                RIGHT, TOP_RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 2, 4,
                RIGHT, TOP_RIGHT, TOP, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 0, 5,
                RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 4, 4,
                RIGHT, TOP, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 2, 5,
                RIGHT, TOP_RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 4, 5,
                RIGHT, TOP_RIGHT, TOP, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 5, 5,
                RIGHT, TOP, TOP_LEFT, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 1, 4,
                RIGHT, TOP, LEFT, BOTTOM, BOTTOM_RIGHT);
    }
    
    /// Cases where three cardinal directions are present, with diagonal variants.
    private static void registerThreeSideCases(SLConnectedTextureRegion[] lookup) {
        SLBlob47ConnectedTextureLayout.put(lookup, 1, 2,
                RIGHT, TOP_RIGHT, TOP, LEFT, BOTTOM_LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 6, 2,
                RIGHT, TOP_RIGHT, LEFT, BOTTOM_LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 4, 2,
                RIGHT, TOP, LEFT, BOTTOM_LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 6, 0,
                RIGHT, TOP, BOTTOM_LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 2, 1,
                RIGHT, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 7, 2,
                RIGHT, TOP_RIGHT, TOP_LEFT, LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 5, 2,
                RIGHT, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 3, 2,
                TOP, TOP_LEFT, LEFT, BOTTOM, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 7, 3,
                TOP_RIGHT, TOP, LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 5, 3,
                TOP, LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 2, 3,
                RIGHT, TOP_RIGHT, TOP, TOP_LEFT, LEFT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 6, 3,
                RIGHT, TOP_RIGHT, TOP, LEFT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 4, 3,
                RIGHT, TOP, TOP_LEFT, LEFT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 6, 1,
                RIGHT, TOP, LEFT);
    }
    
    /// Straight connections and opposite-side connections.
    private static void registerTwoSideCases(SLConnectedTextureRegion[] lookup) {
        SLBlob47ConnectedTextureLayout.put(lookup, 0, 2,
                TOP, BOTTOM);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 2, 0,
                RIGHT, LEFT);
    }
    
    /// L-shapes and similar two-neighbor variants.
    private static void registerStraightCases(SLConnectedTextureRegion[] lookup) {
        SLBlob47ConnectedTextureLayout.put(lookup, 1, 1,
                RIGHT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 4, 0,
                RIGHT, BOTTOM);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 3, 1,
                TOP, TOP_LEFT, BOTTOM_LEFT, BOTTOM);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 5, 0,
                LEFT, BOTTOM);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 3, 3,
                TOP, TOP_LEFT, LEFT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 5, 1,
                TOP, LEFT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 1, 3,
                RIGHT, TOP_RIGHT, TOP);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 4, 1,
                RIGHT, TOP);
    }
    
    /// One-side caps and partial corner cases.
    private static void registerCornerAndEndCases(SLConnectedTextureRegion[] lookup) {
        SLBlob47ConnectedTextureLayout.put(lookup, 7, 0,
                RIGHT, LEFT, BOTTOM_RIGHT);
        
        SLBlob47ConnectedTextureLayout.put(lookup, 7, 1,
                LEFT, BOTTOM_RIGHT);
    }
    
    /// Exactly one cardinal neighbor.
    private static void registerSingleNeighborCases(SLConnectedTextureRegion[] lookup) {
        SLBlob47ConnectedTextureLayout.put(lookup, 0, 1, BOTTOM);
        SLBlob47ConnectedTextureLayout.put(lookup, 1, 0, RIGHT);
        SLBlob47ConnectedTextureLayout.put(lookup, 0, 3, TOP);
        SLBlob47ConnectedTextureLayout.put(lookup, 3, 0, LEFT);
    }
    
    /// No neighbors at all.
    private static void registerIsolatedCase(SLConnectedTextureRegion[] lookup) {
        lookup[SLConnectedTextureMasks.NONE] = SLBlob47ConnectedTextureLayout.region(0, 0);
    }
    
    private static void put(
            SLConnectedTextureRegion[] lookup,
            int gridX,
            int gridY,
            SLConnectedTextureDirection... directions
    ) {
        int mask = SLConnectedTextureMasks.of(directions);
        int normalizedMask = SLConnectedTextureMasks.normalize(mask);
        lookup[normalizedMask] = SLBlob47ConnectedTextureLayout.region(gridX, gridY);
    }
    
    private static SLConnectedTextureRegion region(int gridX, int gridY) {
        float minU = gridX * SLBlob47ConnectedTextureLayout.REGION_WIDTH;
        float minV = gridY * SLBlob47ConnectedTextureLayout.REGION_HEIGHT;
        
        return new SLConnectedTextureRegion(
                minU,
                minV,
                SLBlob47ConnectedTextureLayout.REGION_WIDTH,
                SLBlob47ConnectedTextureLayout.REGION_HEIGHT
        );
    }
}
