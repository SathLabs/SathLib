package dev.satherov.sathlib.client.screen.node.color;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.color.ColorPickerModel;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.node.UILeafNode;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.client.gui.Font;

import java.util.Objects;

///
/// Thin hue strip rendered near the top of the color-picker panel.
///
/// The strip reflects the currently selected hue without replacing the main
/// picker controls or the preview swatch.
///
public final class SLHueStripNode extends UILeafNode<SLHueStripNode> {
    
    private static final int DEFAULT_HEIGHT = 4;
    
    private final ColorPickerModel model;
    
    ///
    /// Creates a hue strip with explicit configuration.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    ///
    private SLHueStripNode(SLModifier modifier, ColorPickerModel model) {
        super(modifier);
        this.model = Objects.requireNonNull(model);
    }
    
    ///
    /// Creates a builder-backed hue strip.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    ///
    /// @return configured hue strip
    ///
    @Builder(builderMethodName = "builder")
    public static SLHueStripNode of(SLModifier modifier, ColorPickerModel model) {
        return new SLHueStripNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNull(model)
        );
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(Math.max(0, availableWidth), SLHueStripNode.DEFAULT_HEIGHT);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        int hueColor = this.model.hueColor();
        context.fill(bounds, 0xFF050608);
        context.outline(bounds, SLColorUtils.multiplyAlpha(context.theme().colors().panelBorder(), 0.65F));
        context.fillVerticalGradient(
                bounds.inset(1),
                SLColorUtils.lerp(0.18F, hueColor, context.theme().colors().white()),
                SLColorUtils.lerp(0.28F, 0xFF050608, hueColor)
        );
    }
}
