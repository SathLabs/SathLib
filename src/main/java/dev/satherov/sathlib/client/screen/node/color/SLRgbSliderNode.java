package dev.satherov.sathlib.client.screen.node.color;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.color.ColorPickerChannel;
import dev.satherov.sathlib.client.screen.color.ColorPickerLayout;
import dev.satherov.sathlib.client.screen.color.ColorPickerModel;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.node.UILeafNode;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.style.UIThemeColors;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

import org.lwjgl.glfw.GLFW;

import java.util.Objects;

///
/// Horizontal gradient slider for one RGB channel.
///
public class SLRgbSliderNode extends UILeafNode<SLRgbSliderNode> {
    
    private final ColorPickerModel model;
    private final ColorPickerChannel channel;
    private final Runnable onCommit;
    
    ///
    /// Creates an RGB slider with explicit configuration.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param channel  selected RGB channel
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    public SLRgbSliderNode(SLModifier modifier, ColorPickerModel model, ColorPickerChannel channel, Runnable onCommit) {
        super(modifier);
        this.model = model;
        this.channel = channel;
        this.onCommit = onCommit;
    }
    
    ///
    /// Creates a builder-backed RGB slider.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param channel  selected RGB channel
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    /// @return configured RGB slider
    ///
    @Builder(builderMethodName = "builder")
    public static SLRgbSliderNode of(
            SLModifier modifier,
            ColorPickerModel model,
            ColorPickerChannel channel,
            Runnable onCommit
    ) {
        return new SLRgbSliderNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNull(model),
                Objects.requireNonNull(channel),
                Objects.requireNonNullElse(onCommit, () -> { })
        );
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(ColorPickerLayout.SIDEBAR_WIDTH, ColorPickerLayout.SLIDER_HEIGHT);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        UIThemeColors colors = context.theme().colors();
        
        context.fill(bounds, colors.insetFill());
        int trackX = bounds.x() + 1;
        int trackY = bounds.y() + 1;
        int trackWidth = Math.max(0, bounds.width() - 2);
        int trackHeight = Math.max(0, bounds.height() - 2);
        int startColor = this.model.channelStartColor(this.channel);
        int endColor = this.model.channelEndColor(this.channel);
        
        for (int column = 0; column < trackWidth; column++) {
            float alpha = column / (float) Math.max(1, trackWidth - 1);
            int color = SLColorUtils.lerp(alpha, startColor, endColor);
            context.fill(new SLBounds(trackX + column, trackY, 1, trackHeight), color);
        }
        
        context.outline(bounds, colors.insetBorder());
        
        int handleX = trackX + Math.round(Math.max(0, trackWidth - 1) * this.model.channelValue(this.channel));
        context.fill(new SLBounds(handleX - 1, bounds.y() - 1, 3, bounds.height() + 2), colors.insetStrong());
        context.fill(new SLBounds(handleX, bounds.y(), 1, bounds.height()), colors.white());
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateValue(event.x());
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (!this.isPressed() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateValue(event.x());
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        this.setPressedState(false);
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateValue(event.x());
        this.onCommit.run();
        return true;
    }
    
    ///
    /// Updates the current channel value from a mouse x coordinate.
    ///
    /// @param mouseX pointer x position
    ///
    private void updateValue(double mouseX) {
        SLBounds bounds = this.getBounds();
        float value = (float) ((mouseX - (bounds.x() + 1)) / Math.max(1.0D, bounds.width() - 3.0D));
        this.model.updateChannel(this.channel, Mth.clamp(value, 0.0F, 1.0F));
    }
}
