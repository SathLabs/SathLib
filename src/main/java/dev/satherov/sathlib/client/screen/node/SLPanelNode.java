package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;

///
/// Built-in panel container with a skinned background.
///
/// Panels behave like vertical flow containers and add a shared panel visual
/// during rendering.
///
/// - provide a ready-to-use content container for menus and screens
/// - delegate panel visuals to the active skin
///
/// Extend this class when you need a panel with custom decoration or behavior.
///
public class SLPanelNode extends SLFlowNode<SLPanelNode> {
    
    public SLPanelNode() {
        super(SLAxis.VERTICAL);
        this.padding(SLInsets.all(8));
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        context.skin().renderPanel(context, this.getBounds());
    }
}
