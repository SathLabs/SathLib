package dev.satherov.sathlib.client.screen.layout;

///
/// Alignment options for placing a node inside available space.
///
/// Alignments are resolved during layout after measure sizes are known.
///
/// - place content at the start, center, or end of a region
/// - optionally stretch content to fill the region
///
/// This enum is closed.
///
public enum SLAlignment {
    START,
    CENTER,
    END,
    FILL,
    ;
    
    ///
    /// Resolves the final size for this alignment.
    ///
    /// @param available available space on the axis
    /// @param preferred preferred measured size
    ///
    /// @return final assigned size
    ///
    public int resolveSize(int available, int preferred) {
        if (this == SLAlignment.FILL) return Math.max(0, available);
        return Math.min(Math.max(0, preferred), Math.max(0, available));
    }
    
    ///
    /// Resolves the start coordinate for a child inside an aligned region.
    ///
    /// @param start     region start coordinate
    /// @param available available space on the axis
    /// @param size      already resolved child size
    ///
    /// @return child start coordinate
    ///
    public int resolvePosition(int start, int available, int size) {
        return switch (this) {
            case START, FILL -> start;
            case CENTER -> start + ((available - size) / 2);
            case END -> start + (available - size);
        };
    }
}
