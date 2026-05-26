package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

///
/// Base class for nodes that manage child nodes.
///
/// Containers attach and detach their children alongside their own root
/// lifecycle, and they participate in the normal measure, layout, render, and
/// input passes.
///
/// @param <S> concrete container subtype used for fluent setters
///
public abstract class UIContainerNode<S extends UIContainerNode<S>> extends UINode<S> {
    
    private final List<UINode<?>> children = new ArrayList<>();
    
    ///
    /// Creates an empty container with the empty modifier.
    ///
    protected UIContainerNode() {
        this(SLModifier.none(), List.of());
    }
    
    ///
    /// Creates a container with explicit modifier and children.
    ///
    /// @param modifier node modifier
    /// @param children initial child list
    ///
    protected UIContainerNode(@Nullable SLModifier modifier, List<UINode<?>> children) {
        super(modifier);
        this.children.addAll(List.copyOf(Objects.requireNonNullElse(children, List.of())));
    }
    
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
        UINode<?> normalizedChild = Objects.requireNonNull(child);
        this.children.add(normalizedChild);
        if (this.getRoot() != null) {
            normalizedChild.attach(this.getRoot(), this);
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
        return child.getModifier().margin();
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        for (UINode<?> child : this.children) {
            child.attach(root, this);
        }
    }
    
    @Override
    protected void onDetached() {
        for (UINode<?> child : List.copyOf(this.children)) {
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
