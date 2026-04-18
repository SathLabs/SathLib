package dev.satherov.sathlib.client.screen.layout;

///
/// Expresses how a node chooses its width or height.
///
/// Length values are stored in layout specs, used during measure for preferred
/// sizing, and used again during layout for final assignment.
///
/// - describe content, fixed, percentage, and fill sizing
/// - expose fill weights for flow containers
///
/// Use the static factory methods to create new values.
///
/// @param mode  sizing mode
/// @param value stored pixel amount, percent fraction, or fill weight
///
public record SLLength(Mode mode, float value) {
    
    ///
    /// Supported sizing modes.
    ///
    public enum Mode {
        CONTENT,
        FIXED,
        PERCENT,
        FILL,
    }
    
    ///
    /// Uses the node's measured content size.
    ///
    /// @return content length
    ///
    public static SLLength content() {
        return new SLLength(Mode.CONTENT, 0.0F);
    }
    
    ///
    /// Uses a fixed pixel size.
    ///
    /// @param pixels fixed size
    ///
    /// @return fixed length
    ///
    public static SLLength pixels(int pixels) {
        return new SLLength(Mode.FIXED, pixels);
    }
    
    ///
    /// Uses a fraction of the available space.
    ///
    /// @param percent percentage as a fraction between {@code 0.0} and
    ///                               {@code 1.0}
    ///
    /// @return percentage length
    ///
    public static SLLength percent(float percent) {
        return new SLLength(Mode.PERCENT, percent);
    }
    
    ///
    /// Fills remaining space with a default weight of {@code 1.0}.
    ///
    /// @return fill length
    ///
    public static SLLength fill() {
        return SLLength.fill(1.0F);
    }
    
    ///
    /// Fills remaining space using the supplied weight.
    ///
    /// @param weight relative fill weight
    ///
    /// @return weighted fill length
    ///
    public static SLLength fill(float weight) {
        return new SLLength(Mode.FILL, Math.max(0.0F, weight));
    }
    
    ///
    /// Returns whether this length participates in weighted fill.
    ///
    /// @return {@code true} when this is a fill length
    ///
    public boolean isFill() {
        return this.mode == Mode.FILL;
    }
    
    ///
    /// Returns the fill weight for flow distribution.
    ///
    /// @return fill weight, or {@code 0.0} for non-fill lengths
    ///
    public float weight() {
        return this.mode == Mode.FILL ? Math.max(0.0F, this.value) : 0.0F;
    }
    
    ///
    /// Resolves the preferred size used during measurement.
    ///
    /// @param available available size on the axis
    /// @param preferred measured content-driven size
    ///
    /// @return preferred node size
    ///
    public int resolvePreferred(int available, int preferred) {
        return switch (this.mode) {
            case CONTENT, FILL -> preferred;
            case FIXED -> Math.round(this.value);
            case PERCENT -> Math.round(available * this.value);
        };
    }
    
    ///
    /// Resolves the final assigned size during layout.
    ///
    /// @param available available size on the axis
    /// @param preferred previously measured preferred size
    ///
    /// @return final node size
    ///
    public int resolveFinal(int available, int preferred) {
        return switch (this.mode) {
            case CONTENT -> preferred;
            case FIXED -> Math.round(this.value);
            case PERCENT -> Math.round(available * this.value);
            case FILL -> Math.max(0, available);
        };
    }
}
