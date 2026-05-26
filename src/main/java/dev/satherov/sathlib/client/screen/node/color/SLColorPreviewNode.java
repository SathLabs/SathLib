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

import java.util.Objects;

///
/// Preview swatch for the currently selected color.
///
public class SLColorPreviewNode extends UILeafNode<SLColorPreviewNode> {
    
    private final ColorPickerModel model;
    
    ///
    /// Creates a preview swatch with explicit configuration.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    ///
    public SLColorPreviewNode(SLModifier modifier, ColorPickerModel model) {
        super(modifier);
        this.model = model;
    }
    
    ///
    /// Creates a builder-backed preview swatch.
    ///
    /// @param modifier node modifier
    /// @param model    shared color-picker model
    ///
    /// @return configured preview node
    ///
    @Builder(builderMethodName = "builder")
    public static SLColorPreviewNode of(SLModifier modifier, ColorPickerModel model) {
        return new SLColorPreviewNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNull(model)
        );
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(ColorPickerLayout.PREVIEW_SIZE, ColorPickerLayout.PREVIEW_SIZE);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        UIThemeColors colors = context.theme().colors();
        
        context.fill(bounds, colors.insetFill());
        context.outline(bounds, colors.insetBorder());
        
        int innerX = bounds.x() + ((bounds.width() - ColorPickerLayout.PREVIEW_FILL_SIZE) / 2);
        int innerY = bounds.y() + ((bounds.height() - ColorPickerLayout.PREVIEW_FILL_SIZE) / 2);
        context.fill(new SLBounds(innerX, innerY, ColorPickerLayout.PREVIEW_FILL_SIZE, ColorPickerLayout.PREVIEW_FILL_SIZE), this.model.previewColor());
        context.outline(new SLBounds(innerX, innerY, ColorPickerLayout.PREVIEW_FILL_SIZE, ColorPickerLayout.PREVIEW_FILL_SIZE), colors.panelBorder());
    }
}
