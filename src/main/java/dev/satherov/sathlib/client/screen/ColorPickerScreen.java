package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.color.ColorPickerChannel;
import dev.satherov.sathlib.client.screen.color.ColorPickerLayout;
import dev.satherov.sathlib.client.screen.color.ColorPickerModel;
import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.node.SLColumnNode;
import dev.satherov.sathlib.client.screen.node.SLTextFieldNode;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.node.color.SLColorPreviewNode;
import dev.satherov.sathlib.client.screen.node.color.SLHueSliderNode;
import dev.satherov.sathlib.client.screen.node.color.SLRgbSliderNode;
import dev.satherov.sathlib.client.screen.node.color.SLSaturationValueNode;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.IntConsumer;

///
/// Screen implementation that exposes linked HSV, RGB, and hex color controls.
///
/// The screen now delegates nearly all widget behavior to reusable retained-mode
/// nodes so the screen itself is mostly composition and callback wiring.
///
public class ColorPickerScreen extends SLScreen {
    
    private static final Component TITLE = Component.literal("Color Picker");
    private static final String HEX_PLACEHOLDER = "#RRGGBB";
    
    private final ColorPickerModel model;
    private final @Nullable IntConsumer onChanged;
    
    ///
    /// Creates a color picker initialized to red.
    ///
    public ColorPickerScreen() {
        this(0xFF0000, null);
    }
    
    ///
    /// Creates a color picker initialized to the given RGB value.
    ///
    /// @param initialRgb initial RGB color
    ///
    public ColorPickerScreen(int initialRgb) {
        this(initialRgb, null);
    }
    
    ///
    /// Creates a color picker with an optional change callback.
    ///
    /// @param initialRgb initial RGB color
    /// @param onChanged  callback invoked when the current color is committed
    ///
    public ColorPickerScreen(int initialRgb, @Nullable IntConsumer onChanged) {
        super(ColorPickerScreen.TITLE);
        this.model = new ColorPickerModel(initialRgb);
        this.onChanged = onChanged;
    }
    
    @Override
    protected UINode<?> create() {
        return UI.box()
                .modifier(SLModifier.fill())
                .child(
                        UI.panel()
                                .modifier(
                                        SLModifier.none()
                                                .align(SLAlignment.CENTER, SLAlignment.CENTER)
                                                .padding(SLInsets.all(ColorPickerLayout.PANEL_PADDING))
                                )
                                .gap(SLScalar.pixels(ColorPickerLayout.TITLE_GAP))
                                .child(
                                        UI.row()
                                                .gap(SLScalar.pixels(ColorPickerLayout.COLUMN_GAP))
                                                .child(this.createSaturationValueNode())
                                                .child(this.createHueSliderNode())
                                                .child(this.createSidebar())
                                                .build()
                                )
                                .build()
                )
                .build();
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, this.theme().colors().scrim());
    }
    
    ///
    /// Returns the currently selected RGB color.
    ///
    /// @return selected RGB color
    ///
    public int getRgb() {
        return this.model.rgb();
    }
    
    ///
    /// Builds the right-side utility column.
    ///
    /// @return sidebar node
    ///
    private SLColumnNode createSidebar() {
        return UI.column()
                .gap(SLScalar.pixels(ColorPickerLayout.SECTION_GAP))
                .child(this.createPreviewNode())
                .child(this.createChannelSection())
                .child(this.createHexSection())
                .build();
    }
    
    ///
    /// Builds the RGB channel slider section.
    ///
    /// @return RGB slider section
    ///
    private SLColumnNode createChannelSection() {
        return UI.column()
                .gap(SLScalar.pixels(8))
                .child(this.createLabeledChannel(ColorPickerChannel.RED))
                .child(this.createLabeledChannel(ColorPickerChannel.GREEN))
                .child(this.createLabeledChannel(ColorPickerChannel.BLUE))
                .build();
    }
    
    ///
    /// Builds the hex-field section.
    ///
    /// @return hex section
    ///
    private SLColumnNode createHexSection() {
        return UI.column()
                .gap(SLScalar.pixels(4))
                .child(
                        SLTextFieldNode.builder()
                                .modifier(SLModifier.none().size(UI.px(ColorPickerLayout.SIDEBAR_WIDTH), UI.px(ColorPickerLayout.FIELD_HEIGHT)))
                                .placeholder(ColorPickerScreen.HEX_PLACEHOLDER)
                                .maxLength(7)
                                .sanitizer(ColorPickerModel::sanitizeHexValue)
                                .acceptedCodepoint(codepoint -> codepoint == '#' || Character.digit(codepoint, 16) >= 0)
                                .valueState(this.model.hexValue())
                                .onCommit(this::commitHexValue)
                                .build()
                )
                .build();
    }
    
    ///
    /// Builds one labeled RGB channel control.
    ///
    /// @param channel slider channel
    ///
    /// @return labeled channel column
    ///
    private SLColumnNode createLabeledChannel(ColorPickerChannel channel) {
        return UI.column()
                .gap(SLScalar.pixels(4))
                .child(
                        SLRgbSliderNode.builder()
                                .modifier(SLModifier.none().size(UI.px(ColorPickerLayout.SIDEBAR_WIDTH), UI.px(ColorPickerLayout.SLIDER_HEIGHT)))
                                .model(this.model)
                                .channel(channel)
                                .onCommit(this::fireChanged)
                                .build()
                )
                .build();
    }
    
    ///
    /// Builds the saturation/value picker node.
    ///
    /// @return saturation/value node
    ///
    private SLSaturationValueNode createSaturationValueNode() {
        return SLSaturationValueNode.builder()
                .modifier(SLModifier.none().size(UI.px(ColorPickerLayout.SATURATION_VALUE_SIZE), UI.px(ColorPickerLayout.SATURATION_VALUE_SIZE)))
                .model(this.model)
                .onCommit(this::fireChanged)
                .build();
    }
    
    ///
    /// Builds the vertical hue slider node.
    ///
    /// @return hue slider node
    ///
    private SLHueSliderNode createHueSliderNode() {
        return SLHueSliderNode.builder()
                .modifier(SLModifier.none().size(UI.px(ColorPickerLayout.HUE_WIDTH), UI.px(ColorPickerLayout.SATURATION_VALUE_SIZE)))
                .model(this.model)
                .onCommit(this::fireChanged)
                .build();
    }
    
    ///
    /// Builds the preview node.
    ///
    /// @return preview node
    ///
    private SLColorPreviewNode createPreviewNode() {
        return SLColorPreviewNode.builder()
                .modifier(SLModifier.none().size(UI.px(ColorPickerLayout.PREVIEW_SIZE), UI.px(ColorPickerLayout.PREVIEW_SIZE)))
                .model(this.model)
                .build();
    }
    
    ///
    /// Commits a hex-field value back into the shared color model.
    ///
    /// @param value source hex text
    ///
    private void commitHexValue(String value) {
        if (this.model.commitHex(Objects.requireNonNullElse(value, ""))) {
            this.fireChanged();
        }
    }
    
    ///
    /// Fires the optional external change callback with the current RGB value.
    ///
    private void fireChanged() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.model.rgb());
        }
    }
}
