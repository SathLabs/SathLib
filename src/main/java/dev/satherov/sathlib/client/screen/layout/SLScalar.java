package dev.satherov.sathlib.client.screen.layout;

///
/// Represents a scalar value resolved against a reference size.
///
/// Scalars stay attached to layout specs until a node is measured or laid out,
/// at which point they resolve into concrete pixels.
///
/// - express fixed spacing and offsets
/// - express percentage-based spacing and offsets
///
/// Use the factory methods instead of adding subclasses.
///
/// @param mode  resolution mode
/// @param value stored pixel amount or percentage fraction
///
public record SLScalar(Mode mode, float value) {
    
    ///
    /// Resolution modes supported by {@link SLScalar}.
    ///
    public enum Mode {
        /// Resolve to a fixed pixel value.
        FIXED,
        /// Resolve to a fraction of the reference size.
        PERCENT,
    }
    
    ///
    /// Creates a fixed scalar in pixels.
    ///
    /// @param pixels fixed amount
    ///
    /// @return fixed scalar
    ///
    public static SLScalar pixels(int pixels) {
        return new SLScalar(Mode.FIXED, pixels);
    }
    
    ///
    /// Creates a percentage scalar.
    ///
    /// @param percent percentage as a fraction between {@code 0.0} and
    ///                                              {@code 1.0}
    ///
    /// @return percent scalar
    ///
    public static SLScalar percent(float percent) {
        return new SLScalar(Mode.PERCENT, percent);
    }
    
    ///
    /// Returns a zero scalar.
    ///
    /// @return fixed zero scalar
    ///
    public static SLScalar zero() {
        return SLScalar.pixels(0);
    }
    
    ///
    /// Resolves this scalar against a reference size.
    ///
    /// @param reference reference size used for percentage scalars
    ///
    /// @return resolved integer pixel amount
    ///
    public int resolve(int reference) {
        return switch (this.mode) {
            case FIXED -> Math.round(this.value);
            case PERCENT -> Math.round(reference * this.value);
        };
    }
}
