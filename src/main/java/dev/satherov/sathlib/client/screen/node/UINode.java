package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLayoutSpec;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

///
/// Base node for the retained-mode SathLib UI tree.
///
/// Nodes are configured with one immutable {@link SLModifier}, attached to a
/// {@link UIRoot}, measured, laid out, rendered, and finally detached when the
/// screen rebuilds or closes.
///
/// The runtime tree stays mutable where interaction requires it, but layout
/// configuration is always expressed through the modifier rather than through a
/// parallel builder hierarchy.
///
/// @param <S> concrete node subtype used for fluent setters
///
@SuppressWarnings("doclint:missing")
public abstract class UINode<S extends UINode<S>> {
    
    private @Nullable UIContainerNode<?> parent;
    private @Nullable UIRoot root;
    
    private SLModifier modifier;
    private SLMeasuredSize measuredSize = SLMeasuredSize.ZERO;
    private SLBounds bounds = SLBounds.EMPTY;
    
    private boolean visible = true;
    private boolean enabled = true;
    private boolean hovered;
    private boolean pressed;
    private boolean focused;
    
    ///
    /// Creates a node with the empty modifier.
    ///
    protected UINode() {
        this(SLModifier.none());
    }
    
    ///
    /// Creates a node with an explicit modifier.
    ///
    /// @param modifier node modifier
    ///
    protected UINode(@Nullable SLModifier modifier) {
        this.modifier = Objects.requireNonNullElse(modifier, SLModifier.none());
    }
    
    ///
    /// Returns the concrete self type for fluent runtime mutators.
    ///
    /// @return this node cast to its concrete type
    ///
    @SuppressWarnings("unchecked")
    protected final S self() {
        return (S) this;
    }
    
    ///
    /// Returns the parent container, or {@code null} for the root node.
    ///
    /// @return parent container
    ///
    public final @Nullable UIContainerNode<?> getParent() {
        return this.parent;
    }
    
    ///
    /// Returns the owning root, or {@code null} while detached.
    ///
    /// @return owning root
    ///
    public final @Nullable UIRoot getRoot() {
        return this.root;
    }
    
    ///
    /// Returns the current immutable modifier.
    ///
    /// @return node modifier
    ///
    public final SLModifier getModifier() {
        return this.modifier;
    }
    
    ///
    /// Returns the legacy layout-spec view of the current modifier.
    ///
    /// @return compatibility layout spec
    ///
    @Deprecated(forRemoval = false)
    public final SLLayoutSpec getLayoutSpec() {
        return new SLLayoutSpec(
                this.modifier.width(),
                this.modifier.height(),
                this.modifier.margin(),
                this.modifier.padding(),
                this.modifier.horizontalAlignment(),
                this.modifier.verticalAlignment(),
                this.modifier.offsetX(),
                this.modifier.offsetY()
        );
    }
    
    ///
    /// Returns the last measured size.
    ///
    /// @return measured size
    ///
    public final SLMeasuredSize getMeasuredSize() {
        return this.measuredSize;
    }
    
    ///
    /// Returns the resolved bounds.
    ///
    /// @return node bounds
    ///
    public final SLBounds getBounds() {
        return this.bounds;
    }
    
    ///
    /// Returns whether the node is visible.
    ///
    /// @return visibility flag
    ///
    public final boolean isVisible() {
        return this.visible;
    }
    
    ///
    /// Updates the visibility flag.
    ///
    /// @param visible new visibility state
    ///
    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        
        this.visible = visible;
        this.invalidateLayout();
    }
    
    ///
    /// Returns whether the node is enabled for interaction.
    ///
    /// @return enabled flag
    ///
    public final boolean isEnabled() {
        return this.enabled;
    }
    
    ///
    /// Updates the enabled flag.
    ///
    /// @param enabled new enabled state
    ///
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    ///
    /// Returns whether the pointer is currently over the node.
    ///
    /// @return hover flag
    ///
    public final boolean isHovered() {
        return this.hovered;
    }
    
    ///
    /// Returns whether the node is currently pressed.
    ///
    /// @return pressed flag
    ///
    public final boolean isPressed() {
        return this.pressed;
    }
    
    ///
    /// Returns whether the node currently owns focus.
    ///
    /// @return focus flag
    ///
    public final boolean isFocused() {
        return this.focused;
    }
    
    ///
    /// Returns the resolved content bounds after padding is applied.
    ///
    /// @return content bounds
    ///
    public SLBounds getContentBounds() {
        return this.bounds.inset(this.modifier.padding());
    }
    
    ///
    /// Replaces the node modifier and invalidates layout.
    ///
    /// @param modifier new modifier
    ///
    /// @return this node
    ///
    public final S modifier(SLModifier modifier) {
        SLModifier normalized = Objects.requireNonNullElse(modifier, SLModifier.none());
        if (this.modifier.equals(normalized)) {
            return this.self();
        }
        
        this.modifier = normalized;
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Configures the node width through its modifier.
    ///
    /// @param width new width behavior
    ///
    /// @return this node
    ///
    public final S width(SLLength width) {
        return this.modifier(this.modifier.withWidth(width));
    }
    
    ///
    /// Configures the node height through its modifier.
    ///
    /// @param height new height behavior
    ///
    /// @return this node
    ///
    public final S height(SLLength height) {
        return this.modifier(this.modifier.withHeight(height));
    }
    
    ///
    /// Configures both node dimensions through the modifier.
    ///
    /// @param width  new width behavior
    /// @param height new height behavior
    ///
    /// @return this node
    ///
    public final S size(SLLength width, SLLength height) {
        return this.modifier(this.modifier.withSize(width, height));
    }
    
    ///
    /// Configures node margin through the modifier.
    ///
    /// @param margin new outside spacing
    ///
    /// @return this node
    ///
    public final S margin(SLInsets margin) {
        return this.modifier(this.modifier.withMargin(margin));
    }
    
    ///
    /// Configures node padding through the modifier.
    ///
    /// @param padding new inside spacing
    ///
    /// @return this node
    ///
    public final S padding(SLInsets padding) {
        return this.modifier(this.modifier.withPadding(padding));
    }
    
    ///
    /// Configures node alignment through the modifier.
    ///
    /// @param horizontalAlignment x-axis alignment
    /// @param verticalAlignment   y-axis alignment
    ///
    /// @return this node
    ///
    public final S align(SLAlignment horizontalAlignment, SLAlignment verticalAlignment) {
        return this.modifier(this.modifier.withAlignment(horizontalAlignment, verticalAlignment));
    }
    
    ///
    /// Configures node offsets through the modifier.
    ///
    /// @param offsetX x-axis offset
    /// @param offsetY y-axis offset
    ///
    /// @return this node
    ///
    public final S offset(SLScalar offsetX, SLScalar offsetY) {
        return this.modifier(this.modifier.withOffset(offsetX, offsetY));
    }
    
    ///
    /// Marks the node tree as requiring a fresh layout pass.
    ///
    public final void invalidateLayout() {
        if (this.root != null) {
            this.root.invalidateLayout();
        }
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
        this.hovered = false;
        this.pressed = false;
        this.focused = false;
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
            return this.measuredSize;
        }
        
        int clampedWidth = Math.max(0, availableWidth);
        int clampedHeight = Math.max(0, availableHeight);
        int paddingWidth = this.modifier.padding().horizontal(clampedWidth);
        int paddingHeight = this.modifier.padding().vertical(clampedHeight);
        int contentAvailableWidth = Math.max(0, clampedWidth - paddingWidth);
        int contentAvailableHeight = Math.max(0, clampedHeight - paddingHeight);
        
        SLMeasuredSize measuredContent = this.measureContent(font, contentAvailableWidth, contentAvailableHeight);
        int preferredWidth = measuredContent.width() + paddingWidth;
        int preferredHeight = measuredContent.height() + paddingHeight;
        
        this.measuredSize = new SLMeasuredSize(
                Math.max(0, this.modifier.width().resolvePreferred(clampedWidth, preferredWidth)),
                Math.max(0, this.modifier.height().resolvePreferred(clampedHeight, preferredHeight))
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
        this.onLayout(font, this.getContentBounds());
    }
    
    ///
    /// Renders this node and its descendants.
    ///
    /// @param context render context
    ///
    public final void renderTree(SLRenderContext context) {
        if (!this.visible) {
            return;
        }
        
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
