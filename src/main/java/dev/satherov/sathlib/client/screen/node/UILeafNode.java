package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.layout.SLModifier;

import org.jspecify.annotations.Nullable;

///
/// Convenience base class for nodes that never own child nodes.
///
/// Leaf nodes follow the normal {@link UINode} lifecycle without any child
/// management.
///
/// @param <S> concrete leaf subtype used for fluent setters
///
public abstract class UILeafNode<S extends UILeafNode<S>> extends UINode<S> {
    
    ///
    /// Creates a leaf with the empty modifier.
    ///
    protected UILeafNode() {
        this(SLModifier.none());
    }
    
    ///
    /// Creates a leaf with an explicit modifier.
    ///
    /// @param modifier node modifier
    ///
    protected UILeafNode(@Nullable SLModifier modifier) {
        super(modifier);
    }
}
