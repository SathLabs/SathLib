package dev.satherov.sathlib.client.screen.layout;

///
/// Stores semantic spacing for the four edges of a box.
///
/// Insets are part of a node layout spec and resolve into pixels during measure
/// and layout.
///
/// - describe padding and margin without direct coordinate math
/// - resolve horizontal and vertical totals when needed
///
/// This is a value type and is not intended for inheritance.
///
/// @param left   left edge value
/// @param top    top edge value
/// @param right  right edge value
/// @param bottom bottom edge value
///
public record SLInsets(SLScalar left, SLScalar top, SLScalar right, SLScalar bottom) {
    
    ///
    /// Creates equal insets on all sides.
    ///
    /// @param pixels fixed size on all sides
    ///
    /// @return uniform insets
    ///
    public static SLInsets all(int pixels) {
        SLScalar scalar = SLScalar.pixels(pixels);
        return new SLInsets(scalar, scalar, scalar, scalar);
    }
    
    ///
    /// Creates equal insets on all sides.
    ///
    /// @param scalar semantic scalar applied to all sides
    ///
    /// @return uniform insets
    ///
    public static SLInsets all(SLScalar scalar) {
        return new SLInsets(scalar, scalar, scalar, scalar);
    }
    
    ///
    /// Creates insets with shared horizontal and vertical values.
    ///
    /// @param horizontal left and right value in pixels
    /// @param vertical   top and bottom value in pixels
    ///
    /// @return symmetric insets
    ///
    public static SLInsets symmetric(int horizontal, int vertical) {
        return new SLInsets(
                SLScalar.pixels(horizontal),
                SLScalar.pixels(vertical),
                SLScalar.pixels(horizontal),
                SLScalar.pixels(vertical)
        );
    }
    
    ///
    /// Creates fully specified fixed insets.
    ///
    /// @param left   left value in pixels
    /// @param top    top value in pixels
    /// @param right  right value in pixels
    /// @param bottom bottom value in pixels
    ///
    /// @return fixed insets
    ///
    public static SLInsets of(int left, int top, int right, int bottom) {
        return new SLInsets(
                SLScalar.pixels(left),
                SLScalar.pixels(top),
                SLScalar.pixels(right),
                SLScalar.pixels(bottom)
        );
    }
    
    ///
    /// Returns zero insets.
    ///
    /// @return zero insets
    ///
    public static SLInsets zero() {
        return SLInsets.all(0);
    }
    
    ///
    /// Resolves the left inset against a width reference.
    ///
    /// @param widthReference reference width
    ///
    /// @return resolved left inset
    ///
    public int left(int widthReference) {
        return this.left.resolve(widthReference);
    }
    
    ///
    /// Resolves the right inset against a width reference.
    ///
    /// @param widthReference reference width
    ///
    /// @return resolved right inset
    ///
    public int right(int widthReference) {
        return this.right.resolve(widthReference);
    }
    
    ///
    /// Resolves the top inset against a height reference.
    ///
    /// @param heightReference reference height
    ///
    /// @return resolved top inset
    ///
    public int top(int heightReference) {
        return this.top.resolve(heightReference);
    }
    
    ///
    /// Resolves the bottom inset against a height reference.
    ///
    /// @param heightReference reference height
    ///
    /// @return resolved bottom inset
    ///
    public int bottom(int heightReference) {
        return this.bottom.resolve(heightReference);
    }
    
    ///
    /// Resolves the total horizontal inset size.
    ///
    /// @param widthReference reference width
    ///
    /// @return total horizontal inset
    ///
    public int horizontal(int widthReference) {
        return this.left(widthReference) + this.right(widthReference);
    }
    
    ///
    /// Resolves the total vertical inset size.
    ///
    /// @param heightReference reference height
    ///
    /// @return total vertical inset
    ///
    public int vertical(int heightReference) {
        return this.top(heightReference) + this.bottom(heightReference);
    }
}
