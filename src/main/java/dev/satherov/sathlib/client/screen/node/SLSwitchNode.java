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
/// Built-in toggle switch with an optional label.
///
public final class SLSwitchNode extends SLBooleanControlNode<SLSwitchNode> {
    
    private static final int TRACK_WIDTH = 28;
    private static final int TRACK_HEIGHT = 16;
    private static final int KNOB_SIZE = 12;
    private static final int LABEL_GAP = 8;
    private static final int MIN_HEIGHT = 18;
    
    ///
    /// Creates an unchecked switch with no label.
    ///
    public SLSwitchNode() {
        this(SLModifier.none(), Component.empty(), false, null, null, null);
    }
    
    ///
    /// Creates a fully configured switch.
    ///
    /// @param modifier       node modifier
    /// @param text           label text
    /// @param checked        initial checked state
    /// @param checkedState   optional checked-state binding
    /// @param onValueChanged optional live-change callback
    /// @param onCommit       optional commit callback
    ///
    private SLSwitchNode(
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
    /// Creates a builder-backed switch while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier       node modifier
    /// @param text           label text
    /// @param checked        initial checked state
    /// @param checkedState   optional checked-state binding
    /// @param onValueChanged optional live-change callback
    /// @param onCommit       optional commit callback
    ///
    /// @return configured switch node
    ///
    @Builder
    public static SLSwitchNode of(
            SLModifier modifier,
            Component text,
            Boolean checked,
            UIState<Boolean> checkedState,
            Consumer<Boolean> onValueChanged,
            Consumer<Boolean> onCommit
    ) {
        return new SLSwitchNode(
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
        int labelWidth = this.text().getString().isEmpty() ? 0 : font.width(this.text()) + SLSwitchNode.LABEL_GAP;
        return new SLMeasuredSize(SLSwitchNode.TRACK_WIDTH + labelWidth, SLSwitchNode.MIN_HEIGHT);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        boolean hasLabel = !this.text().getString().isEmpty();
        int trackX = hasLabel ? bounds.right() - SLSwitchNode.TRACK_WIDTH : bounds.x();
        SLBounds trackBounds = new SLBounds(trackX, bounds.y() + ((bounds.height() - SLSwitchNode.TRACK_HEIGHT) / 2), SLSwitchNode.TRACK_WIDTH, SLSwitchNode.TRACK_HEIGHT);
        int borderColor = this.isFocused() || this.isHovered()
                ? context.theme().colors().insetStrong()
                : context.theme().colors().insetBorder();
        int offTop = SLColorUtils.lerp(0.20F, context.theme().colors().insetFill(), context.theme().colors().panelFillTop());
        int offBottom = context.theme().colors().insetFill();
        
        if (this.checked()) {
            int onTop = SLColorUtils.lerp(0.28F, context.theme().accentColor(), context.theme().colors().white());
            int onBottom = SLColorUtils.lerp(0.40F, context.theme().colors().panelFillBottom(), context.theme().accentColor());
            context.fillVerticalGradient(trackBounds, onTop, onBottom);
        } else {
            context.fillVerticalGradient(trackBounds, offTop, offBottom);
        }
        context.outline(trackBounds, borderColor);
        
        int knobTravel = trackBounds.width() - SLSwitchNode.KNOB_SIZE - 4;
        int knobX = trackBounds.x() + 2 + (this.checked() ? knobTravel : 0);
        SLBounds knobBounds = new SLBounds(knobX, trackBounds.y() + 2, SLSwitchNode.KNOB_SIZE, SLSwitchNode.KNOB_SIZE);
        context.fillVerticalGradient(
                knobBounds,
                SLColorUtils.lerp(0.25F, context.theme().colors().panelInset(), context.theme().colors().white()),
                context.theme().colors().panelInset()
        );
        context.outline(knobBounds, 0x8A000000);
        
        if (hasLabel) {
            int textY = context.centeredVisualTextY(bounds);
            context.text(this.text(), bounds.x(), textY, context.theme().labelColor(this.isEnabled()), false);
        }
    }
}
