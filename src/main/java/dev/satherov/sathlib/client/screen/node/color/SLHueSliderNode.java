package dev.satherov.sathlib.client.screen.node.color;

import lombok.Builder;

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
/// Vertical hue slider with a full-spectrum strip.
///
public class SLHueSliderNode extends UILeafNode<SLHueSliderNode> {
    
    private final ColorPickerModel model;
    private final Runnable onCommit;
    
    ///
    /// Creates a hue slider with explicit configuration.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    public SLHueSliderNode(SLModifier modifier, ColorPickerModel model, Runnable onCommit) {
        super(modifier);
        this.model = model;
        this.onCommit = onCommit;
    }
    
    ///
    /// Creates a builder-backed hue slider.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    /// @return configured hue slider
    ///
    @Builder(builderMethodName = "builder")
    public static SLHueSliderNode of(SLModifier modifier, ColorPickerModel model, Runnable onCommit) {
        return new SLHueSliderNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNull(model),
                Objects.requireNonNullElse(onCommit, () -> { })
        );
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(ColorPickerLayout.HUE_WIDTH, ColorPickerLayout.SATURATION_VALUE_SIZE);
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
        
        for (int row = 0; row < trackHeight; row++) {
            float hue = row / (float) Math.max(1, trackHeight - 1);
            int color = SLColorUtils.hsvToArgb(hue, 1.0F, 1.0F, 255);
            context.fill(new SLBounds(trackX, trackY + row, trackWidth, 1), color);
        }
        
        context.outline(bounds, colors.insetBorder());
        
        int handleY = trackY + Math.round(Math.max(0, trackHeight - 1) * this.model.hue());
        context.fill(new SLBounds(bounds.x() - 1, handleY - 1, bounds.width() + 2, 3), colors.insetStrong());
        context.fill(new SLBounds(bounds.x(), handleY, bounds.width(), 1), colors.white());
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateHue(event.y());
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (!this.isPressed() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateHue(event.y());
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        this.setPressedState(false);
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateHue(event.y());
        this.onCommit.run();
        return true;
    }
    
    ///
    /// Updates the current hue value from a mouse y coordinate.
    ///
    /// @param mouseY pointer y position
    ///
    private void updateHue(double mouseY) {
        SLBounds bounds = this.getBounds();
        float hue = (float) ((mouseY - (bounds.y() + 1)) / Math.max(1.0D, bounds.height() - 3.0D));
        this.model.setHsv(Mth.clamp(hue, 0.0F, 1.0F), this.model.saturation(), this.model.value());
    }
}
