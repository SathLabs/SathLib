package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.layout.SLAxis;

///
/// Horizontal flow container.
///
/// Rows measure children from left to right and assign final bounds during
/// layout.
///
/// - place child nodes side by side
/// - support semantic gaps, fill sizing, and alignment
///
/// Extend this class when you want a row with extra visuals or behavior.
///
public class SLRowNode extends SLFlowNode<SLRowNode> {
    
    public SLRowNode() {
        super(SLAxis.HORIZONTAL);
    }
}
