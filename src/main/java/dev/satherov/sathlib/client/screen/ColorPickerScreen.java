package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.node.SLColumnNode;
import dev.satherov.sathlib.client.screen.node.SLRowNode;
import dev.satherov.sathlib.client.screen.node.SLStackNode;
import dev.satherov.sathlib.client.screen.node.UILeafNode;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.util.SLColorUtils;
import dev.satherov.sathlib.util.SLMathUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.IntConsumer;

///
/// Screen implementation that exposes linked HSV, RGB, and hex color controls.
///
/// This retained-mode version intentionally mirrors the older picker layout and
/// darker chrome:
///
/// - saturation/value square
/// - hue slider
/// - RGB gradient sliders
/// - preview swatch
/// - hex input field
///
/// The screen keeps the same darker panel palette instead of using the brighter
/// generic widget look.
///
public class ColorPickerScreen extends SLScreen {
    
    private static final Component TITLE = Component.literal("Color Picker");
    private final @Nullable IntConsumer onChanged;
    private @Nullable HexFieldNode hexField;
    private int rgb;
    private float hue;
    private float saturation;
    private float value;
    
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
        this.onChanged = onChanged;
        this.applyRgb(initialRgb & 0xFFFFFF, true);
    }
    
    private static String sanitizeHexValue(@Nullable String value) {
        String digits = SLMathUtils.sanitizeHex(value);
        if (digits.isEmpty()) {
            return "";
        }
        return "#" + digits;
    }
    
    private static void drawInsetBox(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, Palette.SOOT);
        graphics.fillGradient(
                x + 1,
                y + 1,
                x + width - 1,
                y + height - 1,
                Palette.VEIL,
                Palette.CLEAR
        );
    }
    
    private static void drawOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        ColorPickerScreen.drawOutline(graphics, x, y, width, height, color, 1);
    }
    
    private static void drawOutline(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            int color,
            int thickness
    ) {
        graphics.fill(x, y, x + width, y + thickness, color);
        graphics.fill(x, y + height - thickness, x + width, y + height, color);
        graphics.fill(x, y, x + thickness, y + height, color);
        graphics.fill(x + width - thickness, y, x + width, y + height, color);
    }
    
    @Override
    protected UINode<?> create() {
        SLStackNode rootNode = new SLStackNode();
        rootNode.size(SLLength.fill(), SLLength.fill());
        
        PickerPanelNode panelNode = new PickerPanelNode();
        panelNode.align(SLAlignment.CENTER, SLAlignment.CENTER);
        panelNode.size(
                SLLength.pixels(Units.panelWidth()),
                SLLength.pixels(Units.panelHeight())
        );
        panelNode.padding(SLInsets.of(
                Units.PANEL_PADDING,
                Units.PANEL_PADDING + Units.TITLE_HEIGHT,
                Units.PANEL_PADDING,
                Units.PANEL_PADDING
        ));
        
        SLRowNode mainRow = new SLRowNode();
        mainRow.gap(SLScalar.pixels(Units.COLUMN_GAP));
        mainRow.addChild(
                new SaturationValueNode().size(
                        SLLength.pixels(Units.SV_SIZE),
                        SLLength.pixels(Units.SV_SIZE)
                )
        );
        mainRow.addChild(
                new HueSliderNode().size(
                        SLLength.pixels(Units.HUE_WIDTH),
                        SLLength.pixels(Units.SV_SIZE)
                )
        );
        
        SLColumnNode rightColumn = new SLColumnNode();
        rightColumn.size(
                SLLength.pixels(Units.INFO_WIDTH),
                SLLength.pixels(Units.rightColumnHeight())
        );
        rightColumn.gap(SLScalar.pixels(Units.SECTION_GAP));
        rightColumn.addChild(
                new PreviewNode().size(
                        SLLength.pixels(Units.PREVIEW_SIZE),
                        SLLength.pixels(Units.PREVIEW_SIZE)
                )
        );
        
        SLColumnNode sliderColumn = new SLColumnNode();
        sliderColumn.gap(SLScalar.pixels(Units.SLIDER_GAP));
        sliderColumn.addChild(
                new RgbSliderNode(ColorChannel.RED).size(
                        SLLength.pixels(Units.INFO_WIDTH),
                        SLLength.pixels(Units.SLIDER_HEIGHT)
                )
        );
        sliderColumn.addChild(
                new RgbSliderNode(ColorChannel.GREEN).size(
                        SLLength.pixels(Units.INFO_WIDTH),
                        SLLength.pixels(Units.SLIDER_HEIGHT)
                )
        );
        sliderColumn.addChild(
                new RgbSliderNode(ColorChannel.BLUE).size(
                        SLLength.pixels(Units.INFO_WIDTH),
                        SLLength.pixels(Units.SLIDER_HEIGHT)
                )
        );
        rightColumn.addChild(sliderColumn);
        
        HexFieldNode hexFieldNode = new HexFieldNode();
        hexFieldNode.size(
                SLLength.pixels(Units.INFO_WIDTH),
                SLLength.pixels(Units.FIELD_HEIGHT)
        );
        hexFieldNode.setValueSilently(SLMathUtils.rgbToHex(this.getRgb()));
        this.hexField = hexFieldNode;
        rightColumn.addChild(hexFieldNode);
        
        mainRow.addChild(rightColumn);
        panelNode.addChild(mainRow);
        rootNode.addChild(panelNode);
        return rootNode;
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, Palette.SCRIM);
    }
    
    ///
    /// Returns the currently selected RGB color.
    ///
    /// @return selected RGB color
    ///
    public int getRgb() {
        return this.rgb & 0xFFFFFF;
    }
    
    private int getHueColor() {
        return SLColorUtils.argb(
                SLColorUtils.red(SLColorUtils.hsvToRgb(this.hue, 1.0F, 1.0F)),
                SLColorUtils.green(SLColorUtils.hsvToRgb(this.hue, 1.0F, 1.0F)),
                SLColorUtils.blue(SLColorUtils.hsvToRgb(this.hue, 1.0F, 1.0F))
        );
    }
    
    private int getPreviewColor() {
        return SLColorUtils.argb(
                SLColorUtils.red(this.getRgb()),
                SLColorUtils.green(this.getRgb()),
                SLColorUtils.blue(this.getRgb())
        );
    }
    
    private void updateRgbChannel(ColorChannel channel, float value) {
        int channelValue = Math.round(value * 255.0F);
        int rgb = this.getRgb();
        int red = SLColorUtils.red(rgb);
        int green = SLColorUtils.green(rgb);
        int blue = SLColorUtils.blue(rgb);
        
        switch (channel) {
            case RED -> red = channelValue;
            case GREEN -> green = channelValue;
            case BLUE -> blue = channelValue;
        }
        
        this.applyRgb(SLColorUtils.rgb(red, green, blue), true);
    }
    
    private void setHsv(float hue, float saturation, float value) {
        this.hue = Mth.clamp(hue, 0.0F, 1.0F);
        this.saturation = Mth.clamp(saturation, 0.0F, 1.0F);
        this.value = Mth.clamp(value, 0.0F, 1.0F);
        this.rgb = SLColorUtils.hsvToRgb(this.hue, this.saturation, this.value) & 0xFFFFFF;
        this.syncHexFieldFromColor();
    }
    
    private void applyRgb(int rgb, boolean syncHue) {
        this.rgb = rgb & 0xFFFFFF;
        this.syncHsvFromRgb(this.rgb, syncHue);
        this.syncHexFieldFromColor();
    }
    
    private void syncHsvFromRgb(int rgb, boolean syncHue) {
        float[] hsv = SLColorUtils.rgbToHsv(rgb);
        this.saturation = hsv[1];
        this.value = hsv[2];
        
        if (!syncHue) {
            return;
        }
        if (hsv[1] <= 0.0F) {
            return;
        }
        if (hsv[0] == 0.0F && this.hue >= 1.0F) {
            return;
        }
        this.hue = hsv[0];
    }
    
    private void syncHexFieldFromColor() {
        if (this.hexField == null || this.hexField.isFocused()) {
            return;
        }
        this.hexField.setValueSilently(SLMathUtils.rgbToHex(this.getRgb()));
    }
    
    private void commitHexValue(String value) {
        Integer rgb = SLMathUtils.tryPraseToHex(value);
        if (rgb == null) {
            String paddedValue = value + "0".repeat(Math.max(0, 7 - value.length()));
            rgb = SLMathUtils.tryPraseToHex(paddedValue);
        }
        
        if (rgb == null) {
            this.syncHexFieldFromColor();
            return;
        }
        
        this.applyRgb(rgb, true);
        if (this.hexField != null) {
            this.hexField.setValueSilently(SLMathUtils.rgbToHex(this.getRgb()));
        }
        this.fireChanged();
    }
    
    private void fireChanged() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.getRgb());
        }
    }
    
    ///
    /// RGB channel selector used by the gradient sliders.
    ///
    private enum ColorChannel {
        RED,
        GREEN,
        BLUE
    }
    
    ///
    /// Fixed layout constants used to assemble the picker UI.
    ///
    private static final class Units {
        
        private static final int PANEL_PADDING = 16;
        private static final int TITLE_HEIGHT = 16;
        private static final int ACCENT_Y = 8;
        private static final int SV_SIZE = 184;
        private static final int HUE_WIDTH = 18;
        private static final int COLUMN_GAP = 14;
        private static final int INFO_WIDTH = 116;
        private static final int PREVIEW_SIZE = 82;
        private static final int PREVIEW_FILL_SIZE = 72;
        private static final int FIELD_HEIGHT = 18;
        private static final int SLIDER_HEIGHT = 10;
        private static final int SLIDER_GAP = 14;
        private static final int SECTION_GAP = 16;
        private static final int TEXT_PADDING = 4;
        
        private static int panelWidth() {
            return (Units.PANEL_PADDING * 2) + Units.SV_SIZE + Units.HUE_WIDTH + Units.INFO_WIDTH + (Units.COLUMN_GAP * 2);
        }
        
        private static int rightColumnHeight() {
            return Units.PREVIEW_SIZE
                    + Units.SECTION_GAP
                    + (Units.SLIDER_HEIGHT * 3)
                    + (Units.SLIDER_GAP * 2)
                    + Units.SECTION_GAP
                    + Units.FIELD_HEIGHT;
        }
        
        private static int panelHeight() {
            return (Units.PANEL_PADDING * 2) + Units.TITLE_HEIGHT + Math.max(Units.SV_SIZE, Units.rightColumnHeight());
        }
    }
    
    ///
    /// Dark neutral palette copied from the older picker chrome.
    ///
    private static final class Palette {
        
        private static final int SHADOW = 0x24000000;
        private static final int SCRIM = 0x7A07090C;
        private static final int PITCH = 0xFF0B0D10;
        private static final int SOOT = 0xCC080B0F;
        private static final int CHARCOAL = 0xD10D1116;
        private static final int ONYX = 0xF7101419;
        private static final int GRAPHITE = 0xF71A1F26;
        private static final int SLATE = 0xFF232A33;
        private static final int STEEL = 0xFF46515F;
        private static final int STORM = 0xFF6A869F;
        private static final int ASH = 0xFF7F8A96;
        private static final int VEIL = 0x22000000;
        private static final int CLEAR = 0x00000000;
        private static final int PEARL = 0xFFD6DEE7;
        private static final int CLOUD = 0xFFE8EDF3;
        private static final int SNOW = 0xFFFFFFFF;
    }
    
    ///
    /// Panel node that recreates the darker color picker chrome.
    ///
    private final class PickerPanelNode extends SLColumnNode {
        
        @Override
        protected void renderSelf(SLRenderContext context) {
            SLBounds bounds = this.getBounds();
            int trackX = bounds.x() + Units.PANEL_PADDING;
            int trackWidth = Math.max(0, bounds.width() - (Units.PANEL_PADDING * 2));
            int fillWidth = Math.max(0, Math.min(trackWidth, Math.round(trackWidth * ColorPickerScreen.this.hue)));
            
            context.fill(new SLBounds(bounds.x() - 8, bounds.y() - 8, bounds.width() + 16, bounds.height() + 16), Palette.SHADOW);
            context.graphics().fillGradient(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), Palette.GRAPHITE, Palette.ONYX);
            context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, bounds.width() - 2, bounds.height() - 2), Palette.CHARCOAL);
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x(), bounds.y(), bounds.width(), bounds.height(), Palette.STEEL);
            context.fill(new SLBounds(trackX, bounds.y() + Units.ACCENT_Y, trackWidth, 1), Palette.SLATE);
            context.fill(new SLBounds(trackX, bounds.y() + Units.ACCENT_Y, fillWidth, 2), ColorPickerScreen.this.getHueColor());
            context.text(ColorPickerScreen.TITLE, bounds.x() + Units.PANEL_PADDING, bounds.y() + 16, Palette.CLOUD, false);
        }
    }
    
    ///
    /// Two-dimensional picker for saturation and value selection.
    ///
    private final class SaturationValueNode extends UILeafNode<SaturationValueNode> {
        
        @Override
        protected boolean isInputTarget() {
            return true;
        }
        
        @Override
        protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
            return new SLMeasuredSize(Units.SV_SIZE, Units.SV_SIZE);
        }
        
        @Override
        protected void renderSelf(SLRenderContext context) {
            SLBounds bounds = this.getBounds();
            ColorPickerScreen.drawInsetBox(context.graphics(), bounds.x() - 2, bounds.y() - 2, bounds.width() + 4, bounds.height() + 4);
            
            for (int column = 0; column < bounds.width(); column++) {
                float alpha = column / (float) Math.max(1, bounds.width() - 1);
                int columnColor = SLColorUtils.lerp(alpha, Palette.SNOW, ColorPickerScreen.this.getHueColor());
                context.graphics().fillGradient(
                        bounds.x() + column,
                        bounds.y(),
                        bounds.x() + column + 1,
                        bounds.bottom(),
                        columnColor,
                        0xFF000000
                );
            }
            
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, Palette.PITCH);
            
            int cursorX = Math.round(ColorPickerScreen.this.saturation * Math.max(0, bounds.width() - 1));
            int cursorY = Math.round((1.0F - ColorPickerScreen.this.value) * Math.max(0, bounds.height() - 1));
            int innerColor = ColorPickerScreen.this.value > 0.65F && ColorPickerScreen.this.saturation < 0.35F
                    ? Palette.PEARL
                    : Palette.SNOW;
            
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x() + cursorX - 4, bounds.y() + cursorY - 4, 9, 9, Palette.PEARL);
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x() + cursorX - 3, bounds.y() + cursorY - 3, 7, 7, innerColor);
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
            ColorPickerScreen.this.fireChanged();
            return true;
        }
        
        private void updateSelection(double mouseX, double mouseY) {
            SLBounds bounds = this.getBounds();
            float saturation = (float) ((mouseX - bounds.x()) / Math.max(1.0D, bounds.width() - 1.0D));
            float value = 1.0F - (float) ((mouseY - bounds.y()) / Math.max(1.0D, bounds.height() - 1.0D));
            ColorPickerScreen.this.setHsv(ColorPickerScreen.this.hue, saturation, value);
        }
    }
    
    ///
    /// Vertical hue slider with the older saturated strip rendering.
    ///
    private final class HueSliderNode extends UILeafNode<HueSliderNode> {
        
        @Override
        protected boolean isInputTarget() {
            return true;
        }
        
        @Override
        protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
            return new SLMeasuredSize(Units.HUE_WIDTH, Units.SV_SIZE);
        }
        
        @Override
        protected void renderSelf(SLRenderContext context) {
            SLBounds bounds = this.getBounds();
            ColorPickerScreen.drawInsetBox(context.graphics(), bounds.x(), bounds.y(), bounds.width(), bounds.height());
            
            int trackX = bounds.x() + 1;
            int trackY = bounds.y() + 1;
            int trackWidth = Math.max(0, bounds.width() - 2);
            int trackHeight = Math.max(0, bounds.height() - 2);
            for (int row = 0; row < trackHeight; row++) {
                float hue = row / (float) Math.max(1, trackHeight - 1);
                int color = SLColorUtils.hsvToArgb(hue, 1.0F, 1.0F, 255);
                context.fill(new SLBounds(trackX, trackY + row, trackWidth, 1), color);
            }
            
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x(), bounds.y(), bounds.width(), bounds.height(), Palette.PITCH);
            
            int handleY = trackY + Math.round(Math.max(0, trackHeight - 1) * ColorPickerScreen.this.hue);
            context.fill(new SLBounds(bounds.x() - 1, handleY - 1, bounds.width() + 2, 3), Palette.PEARL);
            context.fill(new SLBounds(bounds.x(), handleY, bounds.width(), 1), Palette.SNOW);
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
            ColorPickerScreen.this.fireChanged();
            return true;
        }
        
        private void updateHue(double mouseY) {
            SLBounds bounds = this.getBounds();
            float hue = (float) ((mouseY - (bounds.y() + 1)) / Math.max(1.0D, bounds.height() - 3.0D));
            ColorPickerScreen.this.setHsv(hue, ColorPickerScreen.this.saturation, ColorPickerScreen.this.value);
        }
    }
    
    ///
    /// Horizontal gradient slider for one RGB channel.
    ///
    private final class RgbSliderNode extends UILeafNode<RgbSliderNode> {
        
        private final ColorChannel channel;
        
        private RgbSliderNode(ColorChannel channel) {
            this.channel = Objects.requireNonNull(channel);
        }
        
        @Override
        protected boolean isInputTarget() {
            return true;
        }
        
        @Override
        protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
            return new SLMeasuredSize(Units.INFO_WIDTH, Units.SLIDER_HEIGHT);
        }
        
        @Override
        protected void renderSelf(SLRenderContext context) {
            SLBounds bounds = this.getBounds();
            ColorPickerScreen.drawInsetBox(context.graphics(), bounds.x(), bounds.y(), bounds.width(), bounds.height());
            
            int trackX = bounds.x() + 1;
            int trackY = bounds.y() + 1;
            int trackWidth = Math.max(0, bounds.width() - 2);
            int trackHeight = Math.max(0, bounds.height() - 2);
            int startColor = this.startColor();
            int endColor = this.endColor();
            for (int column = 0; column < trackWidth; column++) {
                float alpha = column / (float) Math.max(1, trackWidth - 1);
                int color = SLColorUtils.lerp(alpha, startColor, endColor);
                context.fill(new SLBounds(trackX + column, trackY, 1, trackHeight), color);
            }
            
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x(), bounds.y(), bounds.width(), bounds.height(), Palette.PITCH);
            
            int handleX = trackX + Math.round(Math.max(0, trackWidth - 1) * this.currentValue());
            context.fill(new SLBounds(handleX - 1, bounds.y() - 1, 3, bounds.height() + 2), Palette.PEARL);
            context.fill(new SLBounds(handleX, bounds.y(), 1, bounds.height()), Palette.SNOW);
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
            ColorPickerScreen.this.fireChanged();
            return true;
        }
        
        private float currentValue() {
            int rgb = ColorPickerScreen.this.getRgb();
            return switch (this.channel) {
                case RED -> SLColorUtils.red(rgb) / 255.0F;
                case GREEN -> SLColorUtils.green(rgb) / 255.0F;
                case BLUE -> SLColorUtils.blue(rgb) / 255.0F;
            };
        }
        
        private int startColor() {
            int rgb = ColorPickerScreen.this.getRgb();
            return switch (this.channel) {
                case RED -> SLColorUtils.argb(0, SLColorUtils.green(rgb), SLColorUtils.blue(rgb));
                case GREEN -> SLColorUtils.argb(SLColorUtils.red(rgb), 0, SLColorUtils.blue(rgb));
                case BLUE -> SLColorUtils.argb(SLColorUtils.red(rgb), SLColorUtils.green(rgb), 0);
            };
        }
        
        private int endColor() {
            int rgb = ColorPickerScreen.this.getRgb();
            return switch (this.channel) {
                case RED -> SLColorUtils.argb(255, SLColorUtils.green(rgb), SLColorUtils.blue(rgb));
                case GREEN -> SLColorUtils.argb(SLColorUtils.red(rgb), 255, SLColorUtils.blue(rgb));
                case BLUE -> SLColorUtils.argb(SLColorUtils.red(rgb), SLColorUtils.green(rgb), 255);
            };
        }
        
        private void updateValue(double mouseX) {
            SLBounds bounds = this.getBounds();
            float value = (float) ((mouseX - (bounds.x() + 1)) / Math.max(1.0D, bounds.width() - 3.0D));
            ColorPickerScreen.this.updateRgbChannel(this.channel, Mth.clamp(value, 0.0F, 1.0F));
        }
    }
    
    ///
    /// Preview swatch for the currently selected color.
    ///
    private final class PreviewNode extends UILeafNode<PreviewNode> {
        
        @Override
        protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
            return new SLMeasuredSize(Units.PREVIEW_SIZE, Units.PREVIEW_SIZE);
        }
        
        @Override
        protected void renderSelf(SLRenderContext context) {
            SLBounds bounds = this.getBounds();
            ColorPickerScreen.drawInsetBox(context.graphics(), bounds.x() - 2, bounds.y() - 2, bounds.width() + 4, bounds.height() + 4);
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, Palette.PITCH);
            
            int innerX = bounds.x() + ((bounds.width() - Units.PREVIEW_FILL_SIZE) / 2);
            int innerY = bounds.y() + ((bounds.height() - Units.PREVIEW_FILL_SIZE) / 2);
            context.fill(new SLBounds(innerX, innerY, Units.PREVIEW_FILL_SIZE, Units.PREVIEW_FILL_SIZE), ColorPickerScreen.this.getPreviewColor());
        }
    }
    
    ///
    /// Single-line hex field used by the picker.
    ///
    private final class HexFieldNode extends UILeafNode<HexFieldNode> {
        
        private String value = "";
        private int cursor;
        private int selectionAnchor;
        private boolean wasFocused;
        
        @Override
        protected boolean isInputTarget() {
            return true;
        }
        
        @Override
        protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
            return new SLMeasuredSize(Units.INFO_WIDTH, Units.FIELD_HEIGHT);
        }
        
        @Override
        protected void tick() {
            if (this.wasFocused && !this.isFocused()) {
                this.commitValue();
            }
            this.wasFocused = this.isFocused();
        }
        
        @Override
        protected void renderSelf(SLRenderContext context) {
            SLBounds bounds = this.getBounds();
            int outlineColor = this.isFocused() ? Palette.PEARL : Palette.STEEL;
            
            ColorPickerScreen.drawInsetBox(context.graphics(), bounds.x(), bounds.y(), bounds.width(), bounds.height());
            ColorPickerScreen.drawOutline(context.graphics(), bounds.x(), bounds.y(), bounds.width(), bounds.height(), outlineColor);
            
            int textX = bounds.x() + Units.TEXT_PADDING;
            int textY = bounds.y() + ((bounds.height() - context.font().lineHeight) / 2);
            String renderText = this.value.isEmpty() ? "#RRGGBB" : this.value;
            int textColor = this.value.isEmpty() ? Palette.ASH : Palette.CLOUD;
            
            if (this.hasSelection() && !this.value.isEmpty()) {
                int selectionStart = this.selectionStart();
                int selectionEnd = this.selectionEnd();
                int highlightX = textX + context.font().width(this.value.substring(0, selectionStart));
                int highlightRight = textX + context.font().width(this.value.substring(0, selectionEnd));
                int highlightColor = 0x66000000 | (Palette.STORM & 0x00FFFFFF);
                context.graphics().fill(highlightX, textY - 1, highlightRight, textY + context.font().lineHeight + 1, highlightColor);
            }
            
            context.graphics().text(context.font(), renderText, textX, textY, textColor, false);
            
            if (this.isFocused()) {
                int caretX = textX + context.font().width(this.value.substring(0, this.cursor));
                context.graphics().fill(caretX, textY, caretX + 1, textY + context.font().lineHeight, Palette.SNOW);
            }
        }
        
        @Override
        public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                return false;
            }
            if (doubleClick) {
                this.selectAll();
            } else {
                this.moveCursorTo(this.cursorAt(event.x()), event.hasShiftDown());
            }
            this.setPressedState(true);
            return true;
        }
        
        @Override
        public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
            if (!this.isPressed() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                return false;
            }
            this.moveCursorTo(this.cursorAt(event.x()), true);
            return true;
        }
        
        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            boolean wasPressed = this.isPressed();
            this.setPressedState(false);
            return wasPressed && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
        }
        
        @Override
        public boolean keyPressed(KeyEvent event) {
            if (!this.isFocused()) {
                return false;
            }
            
            return switch (event.key()) {
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    this.commitValue();
                    yield true;
                }
                case GLFW.GLFW_KEY_ESCAPE -> {
                    if (this.getRoot() != null) {
                        this.getRoot().requestFocus(null);
                    }
                    yield true;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    this.deleteText(-1);
                    yield true;
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    this.deleteText(1);
                    yield true;
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    this.moveCursorBy(-1, event.hasShiftDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    this.moveCursorBy(1, event.hasShiftDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_HOME -> {
                    this.moveCursorTo(0, event.hasShiftDown());
                    yield true;
                }
                case GLFW.GLFW_KEY_END -> {
                    this.moveCursorTo(this.value.length(), event.hasShiftDown());
                    yield true;
                }
                default -> {
                    if (event.isSelectAll()) {
                        this.selectAll();
                        yield true;
                    }
                    if (event.isCopy()) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.highlightedText());
                        yield true;
                    }
                    if (event.isCut()) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.highlightedText());
                        if (this.hasSelection()) {
                            this.insertText("");
                        }
                        yield true;
                    }
                    if (event.isPaste()) {
                        this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                        yield true;
                    }
                    yield false;
                }
            };
        }
        
        @Override
        public boolean charTyped(CharacterEvent event) {
            if (!this.isFocused()) {
                return false;
            }
            int codepoint = event.codepoint();
            if (codepoint != '#' && Character.digit(codepoint, 16) < 0) {
                return false;
            }
            this.insertText(new String(Character.toChars(codepoint)));
            return true;
        }
        
        private void commitValue() {
            ColorPickerScreen.this.commitHexValue(this.value);
        }
        
        private void setValueSilently(String value) {
            this.value = ColorPickerScreen.sanitizeHexValue(value);
            this.cursor = this.value.length();
            this.selectionAnchor = this.cursor;
        }
        
        private void insertText(String insertedText) {
            int selectionStart = this.selectionStart();
            int selectionEnd = this.selectionEnd();
            String combinedValue = this.value.substring(0, selectionStart) + insertedText + this.value.substring(selectionEnd);
            this.value = ColorPickerScreen.sanitizeHexValue(combinedValue);
            this.cursor = this.value.length();
            this.selectionAnchor = this.cursor;
        }
        
        private void deleteText(int direction) {
            if (this.hasSelection()) {
                this.insertText("");
                return;
            }
            
            if (this.value.isEmpty()) {
                return;
            }
            
            int selectionStart = this.cursor;
            int selectionEnd = this.cursor;
            if (direction < 0 && this.cursor > 0) {
                selectionStart = this.cursor - 1;
            } else if (direction > 0 && this.cursor < this.value.length()) {
                selectionEnd = this.cursor + 1;
            } else {
                return;
            }
            
            this.value = ColorPickerScreen.sanitizeHexValue(this.value.substring(0, selectionStart) + this.value.substring(selectionEnd));
            this.cursor = Math.min(selectionStart, this.value.length());
            this.selectionAnchor = this.cursor;
        }
        
        private void moveCursorBy(int delta, boolean keepSelection) {
            this.moveCursorTo(this.cursor + delta, keepSelection);
        }
        
        private void moveCursorTo(int position, boolean keepSelection) {
            this.cursor = Mth.clamp(position, 0, this.value.length());
            if (!keepSelection) {
                this.selectionAnchor = this.cursor;
            }
        }
        
        private void selectAll() {
            this.cursor = this.value.length();
            this.selectionAnchor = 0;
        }
        
        private int cursorAt(double mouseX) {
            Font font = Minecraft.getInstance().font;
            int textX = this.getBounds().x() + Units.TEXT_PADDING;
            int relativeX = (int) Math.round(mouseX) - textX;
            if (relativeX <= 0) {
                return 0;
            }
            
            int closestCursor = this.value.length();
            int closestDistance = Integer.MAX_VALUE;
            for (int cursorIndex = 0; cursorIndex <= this.value.length(); cursorIndex++) {
                int cursorX = font.width(this.value.substring(0, cursorIndex));
                int distance = Math.abs(cursorX - relativeX);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestCursor = cursorIndex;
                }
            }
            return closestCursor;
        }
        
        private boolean hasSelection() {
            return this.cursor != this.selectionAnchor;
        }
        
        private int selectionStart() {
            return Math.min(this.cursor, this.selectionAnchor);
        }
        
        private int selectionEnd() {
            return Math.max(this.cursor, this.selectionAnchor);
        }
        
        private String highlightedText() {
            if (!this.hasSelection()) {
                return this.value;
            }
            return this.value.substring(this.selectionStart(), this.selectionEnd());
        }
    }
}
