package dev.satherov.sathlib.client.screen.layout;

///
/// Immutable resolved bounds for a laid out node.
///
/// Bounds are assigned during the layout pass and reused for rendering and hit
/// testing until the next invalidation.
///
/// - store concrete position and size
/// - provide small helpers for clipping and hit testing
///
/// This is a value type and is not intended for inheritance.
///
/// @param x      left coordinate
/// @param y      top coordinate
/// @param width  width in pixels
/// @param height height in pixels
///
public record SLBounds(int x, int y, int width, int height) {
    
    public static final SLBounds EMPTY = new SLBounds(0, 0, 0, 0);
    
    ///
    /// Returns the right edge coordinate.
    ///
    /// @return right edge
    ///
    public int right() {
        return this.x + this.width;
    }
    
    ///
    /// Returns the bottom edge coordinate.
    ///
    /// @return bottom edge
    ///
    public int bottom() {
        return this.y + this.height;
    }
    
    ///
    /// Returns whether a point lies inside this bounds rectangle.
    ///
    /// @param pointX test x coordinate
    /// @param pointY test y coordinate
    ///
    /// @return {@code true} when the point is inside
    ///
    public boolean contains(double pointX, double pointY) {
        return pointX >= this.x && pointX < this.right() && pointY >= this.y && pointY < this.bottom();
    }
    
    ///
    /// Insets this bounds rectangle by resolved semantic spacing.
    ///
    /// @param insets insets to apply
    ///
    /// @return inset bounds, clamped to non-negative size
    ///
    public SLBounds inset(SLInsets insets) {
        int left = insets.left(this.width);
        int top = insets.top(this.height);
        int right = insets.right(this.width);
        int bottom = insets.bottom(this.height);
        int width = Math.max(0, this.width - left - right);
        int height = Math.max(0, this.height - top - bottom);
        return new SLBounds(this.x + left, this.y + top, width, height);
    }
    
    ///
    /// Returns translated bounds with the same size.
    ///
    /// @param offsetX x offset
    /// @param offsetY y offset
    ///
    /// @return translated bounds
    ///
    public SLBounds translate(int offsetX, int offsetY) {
        return new SLBounds(this.x + offsetX, this.y + offsetY, this.width, this.height);
    }
}
