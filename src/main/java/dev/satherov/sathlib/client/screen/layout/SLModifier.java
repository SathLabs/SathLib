package dev.satherov.sathlib.client.screen.layout;

import lombok.Builder;

import java.util.Objects;

///
/// Immutable compose-like layout modifier shared by all retained-mode UI nodes.
///
/// Modifiers carry the layout contract for a node without forcing every widget
/// to repeat width, height, padding, margin, alignment, and offset arguments in
/// a second builder hierarchy.
///
/// Nodes keep one modifier instance and replace it whenever layout-affecting
/// state changes.
///
/// @param width               width behavior
/// @param height              height behavior
/// @param margin              outside spacing
/// @param padding             inside spacing
/// @param horizontalAlignment x-axis alignment inside parent space
/// @param verticalAlignment   y-axis alignment inside parent space
/// @param offsetX             resolved x-axis offset applied after alignment
/// @param offsetY             resolved y-axis offset applied after alignment
///
public record SLModifier(
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
    /// Shared empty modifier instance.
    ///
    public static final SLModifier NONE = new SLModifier(
            SLLength.content(),
            SLLength.content(),
            SLInsets.zero(),
            SLInsets.zero(),
            SLAlignment.START,
            SLAlignment.START,
            SLScalar.zero(),
            SLScalar.zero()
    );
    
    ///
    /// Creates a builder-backed modifier while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param width               width behavior
    /// @param height              height behavior
    /// @param margin              outside spacing
    /// @param padding             inside spacing
    /// @param horizontalAlignment x-axis alignment
    /// @param verticalAlignment   y-axis alignment
    /// @param offsetX             x-axis offset
    /// @param offsetY             y-axis offset
    ///
    /// @return normalized modifier
    ///
    @Builder(builderMethodName = "builder")
    public static SLModifier of(
            SLLength width,
            SLLength height,
            SLInsets margin,
            SLInsets padding,
            SLAlignment horizontalAlignment,
            SLAlignment verticalAlignment,
            SLScalar offsetX,
            SLScalar offsetY
    ) {
        return new SLModifier(
                Objects.requireNonNullElse(width, SLLength.content()),
                Objects.requireNonNullElse(height, SLLength.content()),
                Objects.requireNonNullElse(margin, SLInsets.zero()),
                Objects.requireNonNullElse(padding, SLInsets.zero()),
                Objects.requireNonNullElse(horizontalAlignment, SLAlignment.START),
                Objects.requireNonNullElse(verticalAlignment, SLAlignment.START),
                Objects.requireNonNullElse(offsetX, SLScalar.zero()),
                Objects.requireNonNullElse(offsetY, SLScalar.zero())
        );
    }
    
    ///
    /// Returns the empty modifier.
    ///
    /// @return shared empty modifier
    ///
    public static SLModifier none() {
        return SLModifier.NONE;
    }
    
    ///
    /// Returns a modifier that fills both axes.
    ///
    /// @return fill modifier
    ///
    public static SLModifier fill() {
        return SLModifier.NONE.fillSize();
    }
    
    ///
    /// Returns a copy with a new width behavior.
    ///
    /// @param width new width behavior
    ///
    /// @return updated modifier
    ///
    public SLModifier withWidth(SLLength width) {
        return new SLModifier(
                Objects.requireNonNull(width),
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
    /// Compose-style alias for {@link #withWidth(SLLength)}.
    ///
    /// @param width new width behavior
    ///
    /// @return updated modifier
    ///
    public SLModifier width(SLLength width) {
        return this.withWidth(width);
    }
    
    ///
    /// Returns a copy with a new height behavior.
    ///
    /// @param height new height behavior
    ///
    /// @return updated modifier
    ///
    public SLModifier withHeight(SLLength height) {
        return new SLModifier(
                this.width,
                Objects.requireNonNull(height),
                this.margin,
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Compose-style alias for {@link #withHeight(SLLength)}.
    ///
    /// @param height new height behavior
    ///
    /// @return updated modifier
    ///
    public SLModifier height(SLLength height) {
        return this.withHeight(height);
    }
    
    ///
    /// Returns a copy with both dimensions updated.
    ///
    /// @param width  new width behavior
    /// @param height new height behavior
    ///
    /// @return updated modifier
    ///
    public SLModifier withSize(SLLength width, SLLength height) {
        return new SLModifier(
                Objects.requireNonNull(width),
                Objects.requireNonNull(height),
                this.margin,
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Compose-style alias for {@link #withSize(SLLength, SLLength)}.
    ///
    /// @param width  new width behavior
    /// @param height new height behavior
    ///
    /// @return updated modifier
    ///
    public SLModifier size(SLLength width, SLLength height) {
        return this.withSize(width, height);
    }
    
    ///
    /// Returns a copy with fill sizing on both axes.
    ///
    /// @return updated modifier
    ///
    public SLModifier fillSize() {
        return this.withSize(SLLength.fill(), SLLength.fill());
    }
    
    ///
    /// Compose-style alias for {@link #fillSize()}.
    ///
    /// @return updated modifier
    ///
    public SLModifier fillMaxSize() {
        return this.fillSize();
    }
    
    ///
    /// Returns a copy that fills width only.
    ///
    /// @return updated modifier
    ///
    public SLModifier fillWidth() {
        return this.withWidth(SLLength.fill());
    }
    
    ///
    /// Compose-style alias for {@link #fillWidth()}.
    ///
    /// @return updated modifier
    ///
    public SLModifier fillMaxWidth() {
        return this.fillWidth();
    }
    
    ///
    /// Returns a copy that fills height only.
    ///
    /// @return updated modifier
    ///
    public SLModifier fillHeight() {
        return this.withHeight(SLLength.fill());
    }
    
    ///
    /// Compose-style alias for {@link #fillHeight()}.
    ///
    /// @return updated modifier
    ///
    public SLModifier fillMaxHeight() {
        return this.fillHeight();
    }
    
    ///
    /// Returns a copy with new outside spacing.
    ///
    /// @param margin new margin
    ///
    /// @return updated modifier
    ///
    public SLModifier withMargin(SLInsets margin) {
        return new SLModifier(
                this.width,
                this.height,
                Objects.requireNonNull(margin),
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Compose-style alias for {@link #withMargin(SLInsets)}.
    ///
    /// @param margin new margin
    ///
    /// @return updated modifier
    ///
    public SLModifier margin(SLInsets margin) {
        return this.withMargin(margin);
    }
    
    ///
    /// Returns a copy with new inside spacing.
    ///
    /// @param padding new padding
    ///
    /// @return updated modifier
    ///
    public SLModifier withPadding(SLInsets padding) {
        return new SLModifier(
                this.width,
                this.height,
                this.margin,
                Objects.requireNonNull(padding),
                this.horizontalAlignment,
                this.verticalAlignment,
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Compose-style alias for {@link #withPadding(SLInsets)}.
    ///
    /// @param padding new padding
    ///
    /// @return updated modifier
    ///
    public SLModifier padding(SLInsets padding) {
        return this.withPadding(padding);
    }
    
    ///
    /// Returns a copy with new axis alignment.
    ///
    /// @param horizontalAlignment new horizontal alignment
    /// @param verticalAlignment   new vertical alignment
    ///
    /// @return updated modifier
    ///
    public SLModifier withAlignment(SLAlignment horizontalAlignment, SLAlignment verticalAlignment) {
        return new SLModifier(
                this.width,
                this.height,
                this.margin,
                this.padding,
                Objects.requireNonNull(horizontalAlignment),
                Objects.requireNonNull(verticalAlignment),
                this.offsetX,
                this.offsetY
        );
    }
    
    ///
    /// Compose-style alias for {@link #withAlignment(SLAlignment, SLAlignment)}.
    ///
    /// @param horizontalAlignment new horizontal alignment
    /// @param verticalAlignment   new vertical alignment
    ///
    /// @return updated modifier
    ///
    public SLModifier align(SLAlignment horizontalAlignment, SLAlignment verticalAlignment) {
        return this.withAlignment(horizontalAlignment, verticalAlignment);
    }
    
    ///
    /// Returns a copy with new offsets.
    ///
    /// @param offsetX new x-axis offset
    /// @param offsetY new y-axis offset
    ///
    /// @return updated modifier
    ///
    public SLModifier withOffset(SLScalar offsetX, SLScalar offsetY) {
        return new SLModifier(
                this.width,
                this.height,
                this.margin,
                this.padding,
                this.horizontalAlignment,
                this.verticalAlignment,
                Objects.requireNonNull(offsetX),
                Objects.requireNonNull(offsetY)
        );
    }
    
    ///
    /// Compose-style alias for {@link #withOffset(SLScalar, SLScalar)}.
    ///
    /// @param offsetX new x-axis offset
    /// @param offsetY new y-axis offset
    ///
    /// @return updated modifier
    ///
    public SLModifier offset(SLScalar offsetX, SLScalar offsetY) {
        return this.withOffset(offsetX, offsetY);
    }
}
