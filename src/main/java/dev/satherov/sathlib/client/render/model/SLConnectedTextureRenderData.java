package dev.satherov.sathlib.client.render.model;

import net.minecraft.core.Direction;

import java.util.Arrays;

/// Per-block render cache storing resolved target variants and neighbor masks.
public final class SLConnectedTextureRenderData {
    
    private final String[] variants;
    private final int[][] masks;
    
    /// Creates immutable render data for one baked block model instance.
    ///
    /// @param variants resolved variant ids by target index
    /// @param masks    resolved face masks by target index and face ordinal
    public SLConnectedTextureRenderData(String[] variants, int[][] masks) {
        this.variants = Arrays.copyOf(variants, variants.length);
        this.masks = new int[masks.length][];
        for (int i = 0; i < masks.length; i++) {
            this.masks[i] = Arrays.copyOf(masks[i], masks[i].length);
        }
    }
    
    /// Returns the resolved variant id for a target.
    ///
    /// @param targetIndex target index
    ///
    /// @return resolved variant id
    public String variant(int targetIndex) {
        return this.variants[targetIndex];
    }
    
    /// Returns the resolved face mask for a target.
    ///
    /// @param targetIndex target index
    /// @param face        rendered face
    ///
    /// @return resolved connected-texture mask
    public @SLConnectedTextureMask int mask(int targetIndex, Direction face) {
        return this.masks[targetIndex][face.ordinal()];
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SLConnectedTextureRenderData other)) {
            return false;
        }
        return Arrays.equals(this.variants, other.variants) && Arrays.deepEquals(this.masks, other.masks);
    }
    
    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(this.variants) + Arrays.deepHashCode(this.masks);
    }
}
