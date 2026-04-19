package dev.satherov.sathlib.common.menu.logic;

import org.jspecify.annotations.Nullable;

import java.util.List;

///
/// Base node for a retained menu-logic tree.
///
/// Menu nodes describe what slots exist and how they are grouped, but they do
/// not describe any client-side coordinates. The actual slot placement belongs
/// to the menu screen.
///
/// - provide parent linkage for menu logic trees
/// - support container and slot leaf variants
/// - keep menu structure explicit and reusable
///
public abstract class SLMenuNode {
    
    private @Nullable SLMenuContainerNode parent;
    
    ///
    /// Creates a detached menu logic node.
    ///
    protected SLMenuNode() { }
    
    ///
    /// Returns the parent container for this node.
    ///
    /// @return parent container, or {@code null} for the root
    ///
    public @Nullable SLMenuContainerNode getParent() {
        return this.parent;
    }
    
    ///
    /// Returns this node's children.
    ///
    /// Leaf nodes return an empty list.
    ///
    /// @return child node view
    ///
    public List<SLMenuNode> getChildren() {
        return List.of();
    }
    
    ///
    /// Attaches this node to a parent container inside a menu tree.
    ///
    /// @param parent parent container, or {@code null} for the root
    ///
    public final void attach(@Nullable SLMenuContainerNode parent) {
        this.parent = parent;
        this.onAttached();
    }
    
    protected void onAttached() { }
}
