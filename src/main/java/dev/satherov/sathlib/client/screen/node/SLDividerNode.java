package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.client.gui.Font;

import java.util.Objects;

///
/// Lightweight decorative divider used to separate sections inside a panel.
///
public final class SLDividerNode extends UILeafNode<SLDividerNode> {
    
    private static final int DEFAULT_LENGTH = 48;
    private static final int DEFAULT_THICKNESS = 1;
    
    private SLAxis axis;
    private int thickness;
    
    ///
    /// Creates a horizontal divider.
    ///
    public SLDividerNode() {
        this(SLModifier.none(), SLAxis.HORIZONTAL, SLDividerNode.DEFAULT_THICKNESS);
    }
    
    ///
    /// Creates a fully configured divider.
    ///
    /// @param modifier  node modifier
    /// @param axis      divider axis
    /// @param thickness divider thickness
    ///
    private SLDividerNode(SLModifier modifier, SLAxis axis, int thickness) {
        super(modifier);
        this.axis = Objects.requireNonNullElse(axis, SLAxis.HORIZONTAL);
        this.thickness = Math.max(1, thickness);
    }
    
    ///
    /// Creates a builder-backed divider while normalizing omitted values to
    /// framework defaults.
    ///
    /// @param modifier  node modifier
    /// @param axis      divider axis
    /// @param thickness divider thickness
    ///
    /// @return configured divider node
    ///
    @Builder
    public static SLDividerNode of(SLModifier modifier, SLAxis axis, Integer thickness) {
        return new SLDividerNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(axis, SLAxis.HORIZONTAL),
                Objects.requireNonNullElse(thickness, SLDividerNode.DEFAULT_THICKNESS)
        );
    }
    
    ///
    /// Sets the divider axis.
    ///
    /// @param axis new divider axis
    ///
    /// @return this divider
    ///
    public SLDividerNode axis(SLAxis axis) {
        this.axis = Objects.requireNonNullElse(axis, SLAxis.HORIZONTAL);
        this.invalidateLayout();
        return this;
    }
    
    ///
    /// Sets the divider thickness.
    ///
    /// @param thickness new thickness
    ///
    /// @return this divider
    ///
    public SLDividerNode thickness(int thickness) {
        this.thickness = Math.max(1, thickness);
        this.invalidateLayout();
        return this;
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        if (this.axis == SLAxis.VERTICAL) return new SLMeasuredSize(this.thickness + 2, SLDividerNode.DEFAULT_LENGTH);
        return new SLMeasuredSize(SLDividerNode.DEFAULT_LENGTH, this.thickness + 2);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        int shadowColor = SLColorUtils.lerp(0.55F, context.theme().colors().insetBorder(), context.theme().colors().panelFillBottom());
        int highlightColor = SLColorUtils.lerp(0.35F, context.theme().colors().panelFillTop(), context.theme().colors().white());
        
        if (this.axis == SLAxis.VERTICAL) {
            int x = bounds.x() + (bounds.width() / 2) - (this.thickness / 2);
            context.fill(new SLBounds(x, bounds.y(), this.thickness, bounds.height()), shadowColor);
            context.fill(new SLBounds(x + this.thickness, bounds.y(), 1, bounds.height()), highlightColor);
            return;
        }
        
        int y = bounds.y() + (bounds.height() / 2) - (this.thickness / 2);
        context.fill(new SLBounds(bounds.x(), y, bounds.width(), this.thickness), shadowColor);
        context.fill(new SLBounds(bounds.x(), y + this.thickness, bounds.width(), 1), highlightColor);
    }
}
