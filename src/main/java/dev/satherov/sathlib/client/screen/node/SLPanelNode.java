package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;
import lombok.Singular;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;

import java.util.List;
import java.util.Objects;

///
/// Built-in panel container with a themed background.
///
/// Panels behave like vertical flow containers and add a shared surface visual
/// during rendering.
///
public class SLPanelNode extends SLFlowNode<SLPanelNode> {
    
    ///
    /// Creates a vertical panel container with default padding.
    ///
    public SLPanelNode() {
        super(SLModifier.none().withPadding(SLInsets.all(8)), SLAxis.VERTICAL, List.of(), SLScalar.zero(), SLAlignment.START);
    }
    
    ///
    /// Creates a fully configured panel.
    ///
    /// @param modifier          node modifier
    /// @param children          initial child list
    /// @param gap               semantic child gap
    /// @param mainAxisAlignment unused-space alignment on the vertical axis
    ///
    protected SLPanelNode(
            SLModifier modifier,
            List<UINode<?>> children,
            SLScalar gap,
            SLAlignment mainAxisAlignment
    ) {
        super(modifier, SLAxis.VERTICAL, children, gap, mainAxisAlignment);
    }
    
    ///
    /// Creates a builder-backed panel while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier          node modifier
    /// @param children          initial child list
    /// @param gap               semantic child gap
    /// @param mainAxisAlignment unused-space alignment on the vertical axis
    ///
    /// @return configured panel node
    ///
    @Builder
    public static SLPanelNode of(
            SLModifier modifier,
            @Singular("child") List<UINode<?>> children,
            SLScalar gap,
            SLAlignment mainAxisAlignment
    ) {
        return new SLPanelNode(
                Objects.requireNonNullElse(modifier, SLModifier.none().withPadding(SLInsets.all(8))),
                Objects.requireNonNullElse(children, List.of()),
                Objects.requireNonNullElse(gap, SLScalar.zero()),
                Objects.requireNonNullElse(mainAxisAlignment, SLAlignment.START)
        );
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        context.theme().renderPanel(context, this.getBounds());
    }
}
