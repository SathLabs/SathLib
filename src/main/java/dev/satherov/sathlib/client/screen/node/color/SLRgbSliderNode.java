package dev.satherov.sathlib.client.screen.node.color;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.color.ColorPickerChannel;
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
/// Horizontal gradient slider for one RGB channel.
///
public class SLRgbSliderNode extends SLSliderNode {
    
    private final ColorPickerModel model;
    private final ColorPickerChannel channel;
    
    ///
    /// Creates an RGB slider with explicit configuration.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    /// @param channel  selected RGB channel
    /// @param onCommit callback invoked when the drag interaction commits
    ///
    public SLRgbSliderNode(SLModifier modifier, ColorPickerModel model, ColorPickerChannel channel, Runnable onCommit) {
        super(modifier, SLAxis.HORIZONTAL, false, 0.0F, null, null, ignored -> onCommit.run());
        this.model = model;
        this.channel = channel;
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
    @Builder
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
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(ColorPickerLayout.SIDEBAR_WIDTH, ColorPickerLayout.SLIDER_HEIGHT);
    }
    
    @Override
    protected float getSliderFraction() {
        return this.model.channelValue(this.channel);
    }
    
    ///
    /// Forwards the normalized generic slider value into the selected RGB
    /// channel.
    ///
    @Override
    protected void writeSliderFraction(float value) {
        this.model.updateChannel(this.channel, value);
    }
    
    @Override
    protected void renderTrack(SLRenderContext context, SLBounds trackBounds, float value) {
        int startColor = this.model.channelStartColor(this.channel);
        int endColor = this.model.channelEndColor(this.channel);
        
        for (int column = 0; column < trackBounds.width(); column++) {
            float alpha = column / (float) Math.max(1, trackBounds.width() - 1);
            int color = SLColorUtils.lerp(alpha, startColor, endColor);
            context.fill(new SLBounds(trackBounds.x() + column, trackBounds.y(), 1, trackBounds.height()), color);
        }
    }
}
