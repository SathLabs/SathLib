package dev.satherov.sathlib.client.screen.node;

import lombok.Getter;
import lombok.Setter;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLayoutSpec;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import org.jspecify.annotations.Nullable;

///
/// Base node for the retained-mode SathLib UI tree.
///
/// Nodes are created during screen initialization, attached to a
/// {@link UIRoot}, measured, laid out, rendered, and finally detached when the
/// screen rebuilds or closes.
///
/// - store shared node state such as layout, visibility, and interaction flags
/// - provide measure, layout, render, and input hooks for subclasses
/// - propagate layout invalidation back to the owning root
///
/// Custom components typically extend {@link UILeafNode} or
/// {@link UIContainerNode} and override the protected hooks exposed here.
///
/// @param <S> concrete node subtype used for fluent setters
///
@SuppressWarnings("doclint:missing")
public abstract class UINode<S extends UINode<S>> {
    
    private @Nullable @Getter UIContainerNode<?> parent;
    private @Nullable @Getter UIRoot root;
    
    private @Getter SLLayoutSpec layoutSpec = SLLayoutSpec.defaultSpec();
    private @Getter SLMeasuredSize measuredSize = SLMeasuredSize.ZERO;
    private @Getter SLBounds bounds = SLBounds.EMPTY;
    
    private boolean layoutDirty = true;
    
    private @Getter boolean visible = true;
    private @Getter @Setter boolean enabled = true;
    private @Getter boolean hovered;
    private @Getter boolean pressed;
    private @Getter boolean focused;
    
    protected UINode() { }
    
    @SuppressWarnings("unchecked")
    protected final S self() {
        return (S) this;
    }
    
    ///
    /// Returns the resolved content bounds after padding is applied.
    ///
    /// @return content bounds
    ///
    public SLBounds getContentBounds() {
        return this.bounds.inset(this.layoutSpec.padding());
    }
    
    ///
    /// Updates the visibility flag.
    ///
    /// @param visible new visibility state
    ///
    public void setVisible(boolean visible) {
        if (this.visible == visible) return;
        this.visible = visible;
        this.invalidateLayout();
    }
    
    ///
    /// Configures the node width.
    ///
    /// @param width new width behavior
    ///
    /// @return this node
    ///
    public final S width(SLLength width) {
        this.layoutSpec = this.layoutSpec.withWidth(width);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Configures the node height.
    ///
    /// @param height new height behavior
    ///
    /// @return this node
    ///
    public final S height(SLLength height) {
        this.layoutSpec = this.layoutSpec.withHeight(height);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Configures both node dimensions.
    ///
    /// @param width  new width behavior
    /// @param height new height behavior
    ///
    /// @return this node
    ///
    public final S size(SLLength width, SLLength height) {
        this.layoutSpec = this.layoutSpec.withWidth(width).withHeight(height);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Configures node margin.
    ///
    /// @param margin new outside spacing
    ///
    /// @return this node
    ///
    public final S margin(SLInsets margin) {
        this.layoutSpec = this.layoutSpec.withMargin(margin);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Configures node padding.
    ///
    /// @param padding new inside spacing
    ///
    /// @return this node
    ///
    public final S padding(SLInsets padding) {
        this.layoutSpec = this.layoutSpec.withPadding(padding);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Configures node alignment inside parent space.
    ///
    /// @param horizontalAlignment x-axis alignment
    /// @param verticalAlignment   y-axis alignment
    ///
    /// @return this node
    ///
    public final S align(SLAlignment horizontalAlignment, SLAlignment verticalAlignment) {
        this.layoutSpec = this.layoutSpec.withAlignment(horizontalAlignment, verticalAlignment);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Configures node offsets applied after alignment.
    ///
    /// @param offsetX x-axis offset
    /// @param offsetY y-axis offset
    ///
    /// @return this node
    ///
    public final S offset(SLScalar offsetX, SLScalar offsetY) {
        this.layoutSpec = this.layoutSpec.withOffset(offsetX, offsetY);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Marks the node tree as requiring a fresh layout pass.
    ///
    public final void invalidateLayout() {
        this.layoutDirty = true;
        if (this.root != null) this.root.invalidateLayout();
    }
    
    ///
    /// Attaches this node to a UI root.
    ///
    /// @param root   owning root
    /// @param parent parent container, or {@code null} for the root node
    ///
    public final void attach(UIRoot root, @Nullable UIContainerNode<?> parent) {
        this.root = root;
        this.parent = parent;
        this.onAttached(root);
    }
    
    ///
    /// Detaches this node from its UI root.
    ///
    public final void detach() {
        this.onDetached();
        this.parent = null;
        this.root = null;
    }
    
    ///
    /// Measures this node against available space.
    ///
    /// @param font            active font
    /// @param availableWidth  available width
    /// @param availableHeight available height
    ///
    /// @return measured node size
    ///
    public final SLMeasuredSize measure(Font font, int availableWidth, int availableHeight) {
        if (!this.visible) {
            this.measuredSize = SLMeasuredSize.ZERO;
            this.layoutDirty = false;
            return this.measuredSize;
        }
        
        int clampedWidth = Math.max(0, availableWidth);
        int clampedHeight = Math.max(0, availableHeight);
        int paddingWidth = this.layoutSpec.padding().horizontal(clampedWidth);
        int paddingHeight = this.layoutSpec.padding().vertical(clampedHeight);
        int contentAvailableWidth = Math.max(0, clampedWidth - paddingWidth);
        int contentAvailableHeight = Math.max(0, clampedHeight - paddingHeight);
        
        SLMeasuredSize measuredContent = this.measureContent(font, contentAvailableWidth, contentAvailableHeight);
        int preferredWidth = measuredContent.width() + paddingWidth;
        int preferredHeight = measuredContent.height() + paddingHeight;
        
        this.measuredSize = new SLMeasuredSize(
                Math.max(0, this.layoutSpec.width().resolvePreferred(clampedWidth, preferredWidth)),
                Math.max(0, this.layoutSpec.height().resolvePreferred(clampedHeight, preferredHeight))
        );
        return this.measuredSize;
    }
    
    ///
    /// Assigns final bounds and lets subclasses lay out children or internal
    /// content.
    ///
    /// @param bounds resolved node bounds
    /// @param font   active font
    ///
    public final void layout(SLBounds bounds, Font font) {
        this.bounds = bounds;
        this.layoutDirty = false;
        this.onLayout(font, this.getContentBounds());
    }
    
    ///
    /// Renders this node and its descendants.
    ///
    /// @param context render context
    ///
    public final void renderTree(SLRenderContext context) {
        if (!this.visible) return;
        this.renderSelf(context);
        this.renderChildren(context);
    }
    
    ///
    /// Ticks this node and its descendants.
    ///
    public final void tickTree() {
        this.tick();
        this.tickChildren();
    }
    
    ///
    /// Performs hit testing and returns the deepest interactive node under the
    /// given point.
    ///
    /// @param mouseX test x coordinate
    /// @param mouseY test y coordinate
    ///
    /// @return deepest interactive node, or {@code null}
    ///
    public @Nullable UINode<?> hitTest(double mouseX, double mouseY) {
        if (!this.visible || !this.enabled || !this.bounds.contains(mouseX, mouseY)) {
            return null;
        }
        return this.isInputTarget() ? this : null;
    }
    
    ///
    /// Updates internal hover state.
    ///
    /// @param hovered new hover state
    ///
    public final void setHoveredState(boolean hovered) {
        this.hovered = hovered;
    }
    
    ///
    /// Updates internal pressed state.
    ///
    /// @param pressed new pressed state
    ///
    public final void setPressedState(boolean pressed) {
        this.pressed = pressed;
    }
    
    ///
    /// Updates internal focus state.
    ///
    /// @param focused new focus state
    ///
    public final void setFocusedState(boolean focused) {
        this.focused = focused;
    }
    
    ///
    /// Returns whether this node is eligible for focus.
    ///
    /// @return {@code true} when focusable
    ///
    public boolean isFocusable() {
        return this.isInputTarget();
    }
    
    ///
    /// Returns whether the node participates in hit testing for input routing.
    ///
    /// @return {@code true} when the node can directly receive input
    ///
    protected boolean isInputTarget() {
        return false;
    }
    
    ///
    /// Measures the content box of this node.
    ///
    /// @param font            active font
    /// @param availableWidth  available content width
    /// @param availableHeight available content height
    ///
    /// @return measured content size
    ///
    protected abstract SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight);
    
    ///
    /// Lays out internal content or child nodes.
    ///
    /// @param font          active font
    /// @param contentBounds resolved content bounds
    ///
    protected void onLayout(Font font, SLBounds contentBounds) { }
    
    ///
    /// Renders this node's visuals.
    ///
    /// @param context render context
    ///
    protected void renderSelf(SLRenderContext context) { }
    
    ///
    /// Renders child nodes.
    ///
    /// @param context render context
    ///
    protected void renderChildren(SLRenderContext context) { }
    
    ///
    /// Ticks child nodes.
    ///
    protected void tickChildren() { }
    
    ///
    /// Called when the node is attached to a root.
    ///
    /// @param root owning root
    ///
    protected void onAttached(UIRoot root) { }
    
    ///
    /// Called when the node is detached from its root.
    ///
    protected void onDetached() { }
    
    ///
    /// Per-frame tick hook.
    ///
    protected void tick() { }
    
    ///
    /// Mouse movement hook for the node currently under the pointer.
    ///
    /// @param mouseX mouse x
    /// @param mouseY mouse y
    ///
    /// @return {@code true} when handled
    ///
    public boolean mouseMoved(double mouseX, double mouseY) {
        return false;
    }
    
    ///
    /// Mouse press hook.
    ///
    /// @param event       mouse button event
    /// @param doubleClick whether the click is a double click
    ///
    /// @return {@code true} when handled
    ///
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        this.setPressedState(true);
        return false;
    }
    
    ///
    /// Mouse drag hook.
    ///
    /// @param event  mouse button event
    /// @param deltaX drag delta x
    /// @param deltaY drag delta y
    ///
    /// @return {@code true} when handled
    ///
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        return false;
    }
    
    ///
    /// Mouse release hook.
    ///
    /// @param event mouse button event
    ///
    /// @return {@code true} when handled
    ///
    public boolean mouseReleased(MouseButtonEvent event) {
        this.setPressedState(false);
        return false;
    }
    
    ///
    /// Scroll hook.
    ///
    /// @param mouseX  pointer x
    /// @param mouseY  pointer y
    /// @param scrollX horizontal scroll
    /// @param scrollY vertical scroll
    ///
    /// @return {@code true} when handled
    ///
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }
    
    ///
    /// Key press hook.
    ///
    /// @param event key event
    ///
    /// @return {@code true} when handled
    ///
    public boolean keyPressed(KeyEvent event) {
        return false;
    }
    
    ///
    /// Key release hook.
    ///
    /// @param event key event
    ///
    /// @return {@code true} when handled
    ///
    public boolean keyReleased(KeyEvent event) {
        return false;
    }
    
    ///
    /// Character input hook.
    ///
    /// @param event character event
    ///
    /// @return {@code true} when handled
    ///
    public boolean charTyped(CharacterEvent event) {
        return false;
    }
}
