package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

///
/// Base class for nodes that manage child nodes.
///
/// Containers attach and detach their children alongside their own root
/// lifecycle, and they participate in the normal measure, layout, render, and
/// input passes.
///
/// - own ordered child collections
/// - propagate root attachment and detachment
/// - provide shared child hit testing, ticking, and rendering behavior
///
/// Subclasses usually implement only layout behavior while inheriting the child
/// lifecycle management from this class.
///
public abstract class UIContainerNode<S extends UIContainerNode<S>> extends UINode<S> {
    
    private final List<UINode<?>> children = new ArrayList<>();
    
    ///
    /// Returns an immutable view of the child list.
    ///
    /// @return child view
    ///
    public List<UINode<?>> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    ///
    /// Adds a child node to this container.
    ///
    /// @param child child node to add
    ///
    /// @return this container
    ///
    public final S addChild(UINode<?> child) {
        this.children.add(child);
        if (this.getRoot() != null) {
            child.attach(this.getRoot(), this);
        }
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Removes a child node from this container.
    ///
    /// @param child child node to remove
    ///
    public final void removeChild(UINode<?> child) {
        if (!this.children.remove(child)) {
            return;
        }
        child.detach();
        this.invalidateLayout();
    }
    
    ///
    /// Clears all child nodes.
    ///
    public final void clearChildren() {
        List<UINode<?>> detachedChildren = List.copyOf(this.children);
        this.children.clear();
        for (UINode<?> child : detachedChildren) {
            child.detach();
        }
        this.invalidateLayout();
    }
    
    ///
    /// Resolves a child's margin relative to this container's current content
    /// area.
    ///
    /// @param child child node
    ///
    /// @return resolved margin
    ///
    protected final SLInsets childMargin(UINode<?> child) {
        return child.getLayoutSpec().margin();
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        for (UINode<?> child : this.children) {
            child.attach(root, this);
        }
    }
    
    @Override
    protected void onDetached() {
        List<UINode<?>> detachedChildren = List.copyOf(this.children);
        for (UINode<?> child : detachedChildren) {
            child.detach();
        }
    }
    
    @Override
    protected void renderChildren(SLRenderContext context) {
        for (UINode<?> child : this.children) {
            child.renderTree(context);
        }
    }
    
    @Override
    protected void tickChildren() {
        for (UINode<?> child : this.children) {
            child.tickTree();
        }
    }
    
    @Override
    public @Nullable UINode<?> hitTest(double mouseX, double mouseY) {
        if (!this.isVisible() || !this.isEnabled() || !this.getBounds().contains(mouseX, mouseY)) {
            return null;
        }
        
        for (int childIndex = this.children.size() - 1; childIndex >= 0; childIndex--) {
            UINode<?> child = this.children.get(childIndex);
            UINode<?> hitNode = child.hitTest(mouseX, mouseY);
            if (hitNode != null) {
                return hitNode;
            }
        }
        
        return super.hitTest(mouseX, mouseY);
    }
}
