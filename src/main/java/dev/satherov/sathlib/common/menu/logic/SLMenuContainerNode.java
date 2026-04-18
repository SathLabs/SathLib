package dev.satherov.sathlib.common.menu.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

///
/// Container node for menu logic trees.
///
/// Containers own ordered child nodes and make it possible to build menus as a
/// structured tree instead of a flat sequence of slot registrations.
///
/// - store ordered menu children
/// - attach children into the same logic tree
/// - allow reusable grouped menu sections
///
public class SLMenuContainerNode extends SLMenuNode {
    
    private final List<SLMenuNode> children = new ArrayList<>();
    
    ///
    /// Returns the ordered child nodes.
    ///
    /// @return immutable child list
    ///
    @Override
    public List<SLMenuNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
    
    ///
    /// Adds a child node to this container.
    ///
    /// @param child child node
    ///
    /// @return this container
    ///
    public SLMenuContainerNode addChild(SLMenuNode child) {
        child.attach(this);
        this.children.add(child);
        return this;
    }
}
