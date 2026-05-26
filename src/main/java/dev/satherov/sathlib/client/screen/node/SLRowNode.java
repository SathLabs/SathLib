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
/// Horizontal flow container.
///
/// Rows measure children from left to right and assign final bounds during
/// layout.
///
public class SLRowNode extends SLFlowNode<SLRowNode> {
    
    ///
    /// Creates an empty horizontal flow container.
    ///
    public SLRowNode() {
        super(SLAxis.HORIZONTAL);
    }
    
    ///
    /// Creates a fully configured row.
    ///
    /// @param modifier          node modifier
    /// @param children          initial child list
    /// @param gap               semantic child gap
    /// @param mainAxisAlignment unused-space alignment on the horizontal axis
    ///
    protected SLRowNode(
            SLModifier modifier,
            List<UINode<?>> children,
            SLScalar gap,
            SLAlignment mainAxisAlignment
    ) {
        super(modifier, SLAxis.HORIZONTAL, children, gap, mainAxisAlignment);
    }
    
    ///
    /// Creates a builder-backed row while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier          node modifier
    /// @param children          initial child list
    /// @param gap               semantic child gap
    /// @param mainAxisAlignment unused-space alignment on the horizontal axis
    ///
    /// @return configured row node
    ///
    @Builder(builderMethodName = "builder")
    public static SLRowNode of(
            SLModifier modifier,
            @Singular("child") List<UINode<?>> children,
            SLScalar gap,
            SLAlignment mainAxisAlignment
    ) {
        return new SLRowNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(children, List.of()),
                Objects.requireNonNullElse(gap, SLScalar.zero()),
                Objects.requireNonNullElse(mainAxisAlignment, SLAlignment.START)
        );
    }
}
