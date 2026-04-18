package dev.satherov.sathlib.client.screen.node;

///
/// Convenience base class for nodes that never own child nodes.
///
/// Leaf nodes follow the normal {@link UINode} lifecycle without any child
/// management.
///
/// - make custom one-class widgets simpler to implement
///
/// Extend this class for standalone widgets such as labels, buttons, progress
/// bars, sliders, or bespoke custom controls.
///
public abstract class UILeafNode<S extends UILeafNode<S>> extends UINode<S> { }
