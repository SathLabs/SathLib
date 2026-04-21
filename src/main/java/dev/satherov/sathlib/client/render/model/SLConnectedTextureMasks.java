package dev.satherov.sathlib.client.render.model;

/// Bit flags used by connected-texture layouts and runtime lookup.
public final class SLConnectedTextureMasks {
    
    /// Empty mask with no neighboring connections.
    public static final int NONE = 0;
    
    /// Bit for the local-right neighbor.
    public static final int RIGHT = 1;
    /// Bit for the local top-right neighbor.
    public static final int TOP_RIGHT = 1 << 1;
    /// Bit for the local-top neighbor.
    public static final int TOP = 1 << 2;
    /// Bit for the local top-left neighbor.
    public static final int TOP_LEFT = 1 << 3;
    /// Bit for the local-left neighbor.
    public static final int LEFT = 1 << 4;
    /// Bit for the local bottom-left neighbor.
    public static final int BOTTOM_LEFT = 1 << 5;
    /// Bit for the local-bottom neighbor.
    public static final int BOTTOM = 1 << 6;
    /// Bit for the local bottom-right neighbor.
    public static final int BOTTOM_RIGHT = 1 << 7;
    
    /// Mask containing all supported neighbor bits.
    public static final int ALL =
            SLConnectedTextureMasks.RIGHT
                    | SLConnectedTextureMasks.TOP_RIGHT
                    | SLConnectedTextureMasks.TOP
                    | SLConnectedTextureMasks.TOP_LEFT
                    | SLConnectedTextureMasks.LEFT
                    | SLConnectedTextureMasks.BOTTOM_LEFT
                    | SLConnectedTextureMasks.BOTTOM
                    | SLConnectedTextureMasks.BOTTOM_RIGHT;
    
    private SLConnectedTextureMasks() { }
    
    /// Builds a mask from one or more local directions.
    ///
    /// @param directions directions to include
    ///
    /// @return combined mask
    public static @SLConnectedTextureMask int of(SLConnectedTextureDirection... directions) {
        @SLConnectedTextureMask int mask = SLConnectedTextureMasks.NONE;
        
        for (SLConnectedTextureDirection direction : directions) {
            mask |= direction.bit();
        }
        
        return mask;
    }
    
    /// Returns whether all supplied flags are present in the mask.
    ///
    /// @param mask  mask being tested
    /// @param flags required flags
    ///
    /// @return {@code true} when every flag is present
    public static boolean hasAll(@SLConnectedTextureMask int mask, @SLConnectedTextureMask int flags) {
        return (mask & flags) == flags;
    }
    
    /// Returns whether any supplied flag is present in the mask.
    ///
    /// @param mask  mask being tested
    /// @param flags flags to probe for
    ///
    /// @return {@code true} when at least one flag is present
    public static boolean hasAny(@SLConnectedTextureMask int mask, @SLConnectedTextureMask int flags) {
        return (mask & flags) != 0;
    }
    
    /// Adds flags to a mask.
    ///
    /// @param mask  base mask
    /// @param flags flags to add
    ///
    /// @return updated mask
    public static @SLConnectedTextureMask int add(@SLConnectedTextureMask int mask, @SLConnectedTextureMask int flags) {
        return mask | flags;
    }
    
    /// Removes flags from a mask.
    ///
    /// @param mask  base mask
    /// @param flags flags to remove
    ///
    /// @return updated mask
    public static @SLConnectedTextureMask int remove(@SLConnectedTextureMask int mask, @SLConnectedTextureMask int flags) {
        return mask & ~flags;
    }
    
    /// Clears any bits outside the supported connected-texture range.
    ///
    /// @param mask mask to normalize
    ///
    /// @return normalized mask
    public static @SLConnectedTextureMask int normalize(@SLConnectedTextureMask int mask) {
        return mask & SLConnectedTextureMasks.ALL;
    }
    
    /// Converts a mask to an 8-bit binary string.
    ///
    /// @param mask mask to format
    ///
    /// @return zero-padded binary representation
    public static String toBinaryString(@SLConnectedTextureMask int mask) {
        int normalizedMask = SLConnectedTextureMasks.normalize(mask);
        String binary = Integer.toBinaryString(normalizedMask);
        return "0".repeat(8 - binary.length()) + binary;
    }
}
