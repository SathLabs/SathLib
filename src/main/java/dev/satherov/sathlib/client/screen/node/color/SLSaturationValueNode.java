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

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

import org.lwjgl.glfw.GLFW;

import java.util.Objects;

///
/// Two-dimensional picker for saturation and value selection.
///
public class SLSaturationValueNode extends UILeafNode<SLSaturationValueNode> {
    
    private final ColorPickerModel model;
    private final Runnable onCommit;
    
    ///
    /// Creates a saturation/value control with explicit configuration.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    public SLSaturationValueNode(SLModifier modifier, ColorPickerModel model, Runnable onCommit) {
        super(modifier);
        this.model = model;
        this.onCommit = onCommit;
    }
    
    ///
    /// Creates a builder-backed saturation/value control.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    /// @return configured saturation/value node
    ///
    @Builder(builderMethodName = "builder")
    public static SLSaturationValueNode of(SLModifier modifier, ColorPickerModel model, Runnable onCommit) {
        return new SLSaturationValueNode(
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
        return new SLMeasuredSize(ColorPickerLayout.SATURATION_VALUE_SIZE, ColorPickerLayout.SATURATION_VALUE_SIZE);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        UIThemeColors colors = context.theme().colors();
        
        context.fill(bounds, colors.insetFill());
        for (int column = 0; column < bounds.width(); column++) {
            float alpha = column / (float) Math.max(1, bounds.width() - 1);
            int columnColor = dev.satherov.sathlib.util.SLColorUtils.lerp(alpha, colors.white(), this.model.hueColor());
            context.graphics().fillGradient(
                    bounds.x() + column,
                    bounds.y(),
                    bounds.x() + column + 1,
                    bounds.bottom(),
                    columnColor,
                    0xFF000000
            );
        }
        
        context.outline(bounds, colors.insetBorder());
        
        int cursorX = Math.round(this.model.saturation() * Math.max(0, bounds.width() - 1));
        int cursorY = Math.round((1.0F - this.model.value()) * Math.max(0, bounds.height() - 1));
        int innerColor = this.model.value() > 0.65F && this.model.saturation() < 0.35F
                ? colors.insetStrong()
                : colors.white();
        
        context.outline(new SLBounds(bounds.x() + cursorX - 4, bounds.y() + cursorY - 4, 9, 9), colors.insetStrong());
        context.outline(new SLBounds(bounds.x() + cursorX - 3, bounds.y() + cursorY - 3, 7, 7), innerColor);
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateSelection(event.x(), event.y());
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (!this.isPressed() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateSelection(event.x(), event.y());
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        this.setPressedState(false);
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        this.updateSelection(event.x(), event.y());
        this.onCommit.run();
        return true;
    }
    
    ///
    /// Updates the current saturation/value selection from mouse coordinates.
    ///
    /// @param mouseX pointer x position
    /// @param mouseY pointer y position
    ///
    private void updateSelection(double mouseX, double mouseY) {
        SLBounds bounds = this.getBounds();
        float saturation = (float) ((mouseX - bounds.x()) / Math.max(1.0D, bounds.width() - 1.0D));
        float value = 1.0F - (float) ((mouseY - bounds.y()) / Math.max(1.0D, bounds.height() - 1.0D));
        this.model.setHsv(this.model.hue(), Mth.clamp(saturation, 0.0F, 1.0F), Mth.clamp(value, 0.0F, 1.0F));
    }
}
