package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.layout.SLAxis;

///
/// Vertical flow container.
///
/// Columns measure children from top to bottom and assign final bounds during
/// layout.
///
/// - stack child nodes vertically
/// - support semantic gaps, fill sizing, and alignment
///
/// Extend this class when you want a column with extra visuals or behavior.
///
public class SLColumnNode extends SLFlowNode<SLColumnNode> {
    
    public SLColumnNode() {
        super(SLAxis.VERTICAL);
    }
}
