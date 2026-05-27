package dev.satherov.sathlib.client.screen.node.color;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.color.ColorPickerLayout;
import dev.satherov.sathlib.client.screen.color.ColorPickerModel;
import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.node.SLSliderNode;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.client.gui.Font;

import java.util.Objects;

///
/// Vertical hue slider with a full-spectrum strip.
///
public class SLHueSliderNode extends SLSliderNode {
    
    private final ColorPickerModel model;
    
    ///
    /// Creates a hue slider with explicit configuration.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    public SLHueSliderNode(SLModifier modifier, ColorPickerModel model, Runnable onCommit) {
        super(modifier, SLAxis.VERTICAL, false, 0.0F, null, null, ignored -> onCommit.run());
        this.model = model;
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
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(ColorPickerLayout.HUE_WIDTH, ColorPickerLayout.SATURATION_VALUE_SIZE);
    }
    
    @Override
    protected float getSliderFraction() {
        return this.model.hue();
    }
    
    ///
    /// Forwards the normalized generic slider value into the shared hue model.
    ///
    @Override
    protected void writeSliderFraction(float value) {
        this.model.setHsv(value, this.model.saturation(), this.model.value());
    }
    
    @Override
    protected void renderTrack(SLRenderContext context, SLBounds trackBounds, float value) {
        for (int row = 0; row < trackBounds.height(); row++) {
            float hue = row / (float) Math.max(1, trackBounds.height() - 1);
            int color = SLColorUtils.hsvToArgb(hue, 1.0F, 1.0F, 255);
            context.fill(new SLBounds(trackBounds.x(), trackBounds.y() + row, trackBounds.width(), 1), color);
        }
    }
}
