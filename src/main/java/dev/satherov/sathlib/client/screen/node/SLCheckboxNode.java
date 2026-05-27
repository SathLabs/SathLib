package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.state.UIState;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

///
/// Built-in checkbox control with an optional label.
///
public final class SLCheckboxNode extends SLBooleanControlNode<SLCheckboxNode> {
    
    private static final int BOX_SIZE = 14;
    private static final int LABEL_GAP = 6;
    private static final int MIN_WIDTH = 14;
    private static final int MIN_HEIGHT = 16;
    
    ///
    /// Creates an unchecked checkbox with no label.
    ///
    public SLCheckboxNode() {
        this(SLModifier.none(), Component.empty(), false, null, null, null);
    }
    
    ///
    /// Creates a fully configured checkbox.
    ///
    /// @param modifier       node modifier
    /// @param text           label text
    /// @param checked        initial checked state
    /// @param checkedState   optional checked-state binding
    /// @param onValueChanged optional live-change callback
    /// @param onCommit       optional commit callback
    ///
    private SLCheckboxNode(
            SLModifier modifier,
            Component text,
            boolean checked,
            UIState<Boolean> checkedState,
            Consumer<Boolean> onValueChanged,
            Consumer<Boolean> onCommit
    ) {
        super(modifier, text, checked, checkedState, onValueChanged, onCommit);
    }
    
    ///
    /// Creates a builder-backed checkbox while normalizing omitted values to
    /// framework defaults.
    ///
    /// @param modifier       node modifier
    /// @param text           label text
    /// @param checked        initial checked state
    /// @param checkedState   optional checked-state binding
    /// @param onValueChanged optional live-change callback
    /// @param onCommit       optional commit callback
    ///
    /// @return configured checkbox node
    ///
    @Builder
    public static SLCheckboxNode of(
            SLModifier modifier,
            Component text,
            Boolean checked,
            UIState<Boolean> checkedState,
            Consumer<Boolean> onValueChanged,
            Consumer<Boolean> onCommit
    ) {
        return new SLCheckboxNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(text, Component.empty()),
                Objects.requireNonNullElse(checked, false),
                checkedState,
                onValueChanged,
                onCommit
        );
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int labelWidth = this.text().getString().isEmpty() ? 0 : font.width(this.text()) + SLCheckboxNode.LABEL_GAP;
        return new SLMeasuredSize(Math.max(SLCheckboxNode.MIN_WIDTH, SLCheckboxNode.BOX_SIZE + labelWidth), SLCheckboxNode.MIN_HEIGHT);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        SLBounds boxBounds = new SLBounds(bounds.x(), bounds.y() + ((bounds.height() - SLCheckboxNode.BOX_SIZE) / 2), SLCheckboxNode.BOX_SIZE, SLCheckboxNode.BOX_SIZE);
        int borderColor = this.isFocused() || this.isHovered()
                ? context.theme().colors().insetStrong()
                : context.theme().colors().insetBorder();
        int fillTop = SLColorUtils.lerp(0.15F, context.theme().colors().insetFill(), context.theme().colors().panelFillTop());
        int fillBottom = context.theme().colors().insetFill();
        
        context.fillVerticalGradient(boxBounds, fillTop, fillBottom);
        context.outline(boxBounds, borderColor);
        
        if (this.checked()) {
            SLBounds markBounds = boxBounds.inset(2);
            int accentTop = SLColorUtils.lerp(0.28F, context.theme().accentColor(), context.theme().colors().white());
            int accentBottom = SLColorUtils.lerp(0.42F, context.theme().colors().panelFillBottom(), context.theme().accentColor());
            context.fillVerticalGradient(markBounds, accentTop, accentBottom);
        }
        
        if (!this.text().getString().isEmpty()) {
            int textX = boxBounds.right() + SLCheckboxNode.LABEL_GAP;
            int textY = context.centeredVisualTextY(bounds);
            context.text(this.text(), textX, textY, context.theme().labelColor(this.isEnabled()), false);
        }
    }
}
