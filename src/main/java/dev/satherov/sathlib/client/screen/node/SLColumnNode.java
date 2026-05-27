package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;
import lombok.Singular;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;

import java.util.List;
import java.util.Objects;

///
/// Vertical flow container.
///
/// Columns measure children from top to bottom and assign final bounds during
/// layout.
///
public class SLColumnNode extends SLFlowNode<SLColumnNode> {
    
    ///
    /// Creates an empty vertical flow container.
    ///
    public SLColumnNode() {
        super(SLAxis.VERTICAL);
    }
    
    ///
    /// Creates a fully configured column.
    ///
    /// @param modifier          node modifier
    /// @param children          initial child list
    /// @param gap               semantic child gap
    /// @param mainAxisAlignment unused-space alignment on the vertical axis
    ///
    protected SLColumnNode(
            SLModifier modifier,
            List<UINode<?>> children,
            SLScalar gap,
            SLAlignment mainAxisAlignment
    ) {
        super(modifier, SLAxis.VERTICAL, children, gap, mainAxisAlignment);
    }
    
    ///
    /// Creates a builder-backed column while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier          node modifier
    /// @param children          initial child list
    /// @param gap               semantic child gap
    /// @param mainAxisAlignment unused-space alignment on the vertical axis
    ///
    /// @return configured column node
    ///
    @Builder
    public static SLColumnNode of(
            SLModifier modifier,
            @Singular("child") List<UINode<?>> children,
            SLScalar gap,
            SLAlignment mainAxisAlignment
    ) {
        return new SLColumnNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(children, List.of()),
                Objects.requireNonNullElse(gap, SLScalar.zero()),
                Objects.requireNonNullElse(mainAxisAlignment, SLAlignment.START)
        );
    }
}
