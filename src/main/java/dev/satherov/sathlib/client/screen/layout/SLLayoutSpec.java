package dev.satherov.sathlib.client.screen.layout;

///
/// Immutable semantic layout configuration shared by all UI nodes.
///
/// Each node stores one layout spec and replaces it whenever a layout-affecting
/// property changes.
///
/// - group size, spacing, alignment, and offset settings
/// - provide immutable "with" methods for node configuration
///
/// Add more fields here as the retained-mode layout vocabulary grows.
///
/// @param width               width behavior
/// @param height              height behavior
/// @param margin              outside spacing
/// @param padding             inside spacing
/// @param horizontalAlignment x-axis alignment inside parent space
/// @param verticalAlignment   y-axis alignment inside parent space
/// @param offsetX             resolved after alignment on the x-axis
/// @param offsetY             resolved after alignment on the y-axis
///
public record SLLayoutSpec(
        SLLength width,
        SLLength height,
        SLInsets margin,
        SLInsets padding,
        SLAlignment horizontalAlignment,
        SLAlignment verticalAlignment,
        SLScalar offsetX,
        SLScalar offsetY
) {
    
    ///
    /// Returns the default semantic layout configuration.
    ///
    /// @return default layout spec
    ///
    public static SLLayoutSpec defaultSpec() {
        return new SLLayoutSpec(
                SLLength.content(),
                SLLength.content(),
                SLInsets.zero(),
                SLInsets.zero(),
                SLAlignment.START,
                SLAlignment.START,
                SLScalar.zero(),
                SLScalar.zero()
        );
    }
    
    ///
    /// Returns a copy with a new width behavior.
    ///
    /// @param width new width behavior
    ///
    /// @return updated spec
    ///
    public SLLayoutSpec withWidth(SLLength width) {
        return new SLLayoutSpec(
                width,
                this.height,
                this.margin,
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Returns a copy with a new height behavior.
    ///
    /// @param height new height behavior
    ///
    /// @return updated spec
    ///
    public SLLayoutSpec withHeight(SLLength height) {
        return new SLLayoutSpec(
                this.width,
                height,
                this.margin,
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Returns a copy with new outside spacing.
    ///
    /// @param margin new margin
    ///
    /// @return updated spec
    ///
    public SLLayoutSpec withMargin(SLInsets margin) {
        return new SLLayoutSpec(
                this.width,
                this.height,
                margin,
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Returns a copy with new inside spacing.
    ///
    /// @param padding new padding
    ///
    /// @return updated spec
    ///
    public SLLayoutSpec withPadding(SLInsets padding) {
        return new SLLayoutSpec(
                this.width,
                this.height,
                this.margin,
                padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Returns a copy with new axis alignment.
    ///
    /// @param horizontalAlignment new horizontal alignment
    /// @param verticalAlignment   new vertical alignment
    ///
    /// @return updated spec
    ///
    public SLLayoutSpec withAlignment(SLAlignment horizontalAlignment, SLAlignment verticalAlignment) {
        return new SLLayoutSpec(
                this.width,
                this.height,
                this.margin,
                this.padding,
                horizontalAlignment,
                verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Returns a copy with new semantic offsets.
    ///
    /// @param offsetX x-axis offset
    /// @param offsetY y-axis offset
    ///
    /// @return updated spec
    ///
    public SLLayoutSpec withOffset(SLScalar offsetX, SLScalar offsetY) {
        return new SLLayoutSpec(
                this.width,
                this.height,
                this.margin,
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                offsetX,
                offsetY
        );
    }
}
