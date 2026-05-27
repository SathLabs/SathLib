package dev.satherov.sathlib.client.screen.color;

import dev.satherov.sathlib.client.screen.state.UIState;
import dev.satherov.sathlib.util.SLColorUtils;
import dev.satherov.sathlib.util.SLMathUtils;

import net.minecraft.util.Mth;

///
/// Shared state model for the retained-mode color picker.
///
/// The model owns the linked RGB, HSV, and display-hex values so the screen and
/// its child controls can stay small and compositional.
///
public final class ColorPickerModel {
    
    private final UIState<String> hexValue = new UIState<>("#000000");
    
    private int rgb;
    private float hue;
    private float saturation;
    private float value;
    
    ///
    /// Creates a model initialized to the given RGB value.
    ///
    /// @param initial initial RGB color
    ///
    public ColorPickerModel(int initial) {
        this.applyRgb(initial & 0xFFFFFF, true);
    }
    
    ///
    /// Sanitizes a user-visible hex value.
    ///
    /// @param value source value
    ///
    /// @return sanitized display value
    ///
    public static String sanitizeHexValue(String value) {
        String digits = SLMathUtils.sanitizeHex(value);
        if (digits.isEmpty()) return "";
        return "#" + digits;
    }
    
    ///
    /// Returns the current packed RGB value.
    ///
    /// @return current RGB color
    ///
    public int rgb() {
        return this.rgb & 0xFFFFFF;
    }
    
    ///
    /// Returns the current hue in the normalized {@code [0, 1]} range.
    ///
    /// @return current hue
    ///
    public float hue() {
        return this.hue;
    }
    
    ///
    /// Returns the current saturation in the normalized {@code [0, 1]} range.
    ///
    /// @return current saturation
    ///
    public float saturation() {
        return this.saturation;
    }
    
    ///
    /// Returns the current value in the normalized {@code [0, 1]} range.
    ///
    /// @return current value
    ///
    public float value() {
        return this.value;
    }
    
    ///
    /// Returns the synchronized display-hex state.
    ///
    /// @return observable hex state
    ///
    public UIState<String> hexValue() {
        return this.hexValue;
    }
    
    ///
    /// Returns the fully opaque hue color.
    ///
    /// @return packed ARGB hue color
    ///
    public int hueColor() {
        return SLColorUtils.argb(
                SLColorUtils.red(SLColorUtils.hsvToRgb(this.hue, 1.0F, 1.0F)),
                SLColorUtils.green(SLColorUtils.hsvToRgb(this.hue, 1.0F, 1.0F)),
                SLColorUtils.blue(SLColorUtils.hsvToRgb(this.hue, 1.0F, 1.0F))
        );
    }
    
    ///
    /// Returns the fully opaque preview color.
    ///
    /// @return packed ARGB preview color
    ///
    public int previewColor() {
        return SLColorUtils.argb(
                SLColorUtils.red(this.rgb()),
                SLColorUtils.green(this.rgb()),
                SLColorUtils.blue(this.rgb())
        );
    }
    
    ///
    /// Updates one RGB channel through a normalized slider value.
    ///
    /// @param channel selected RGB channel
    /// @param value   normalized channel value
    ///
    public void updateChannel(ColorPickerChannel channel, float value) {
        int channelValue = Math.round(Mth.clamp(value, 0.0F, 1.0F) * 255.0F);
        int rgb = this.rgb();
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
    
    ///
    /// Applies the given HSV color and synchronizes RGB and hex state.
    ///
    /// @param hue        normalized hue
    /// @param saturation normalized saturation
    /// @param value      normalized value
    ///
    public void setHsv(float hue, float saturation, float value) {
        this.hue = Mth.clamp(hue, 0.0F, 1.0F);
        this.saturation = Mth.clamp(saturation, 0.0F, 1.0F);
        this.value = Mth.clamp(value, 0.0F, 1.0F);
        this.rgb = SLColorUtils.hsvToRgb(this.hue, this.saturation, this.value) & 0xFFFFFF;
        this.syncHexValue();
    }
    
    ///
    /// Applies a packed RGB color and synchronizes HSV and hex state.
    ///
    /// @param rgb     packed RGB color
    /// @param syncHue whether a grayscale update is allowed to preserve the old
    ///                hue
    ///
    public void applyRgb(int rgb, boolean syncHue) {
        this.rgb = rgb & 0xFFFFFF;
        this.syncHsvFromRgb(this.rgb, syncHue);
        this.syncHexValue();
    }
    
    ///
    /// Commits a hex string into the current color.
    ///
    /// @param value source hex text
    ///
    /// @return {@code true} when the value parsed successfully
    ///
    public boolean commitHex(String value) {
        Integer rgb = SLMathUtils.tryPraseToHex(value);
        if (rgb == null) {
            String paddedValue = value + "0".repeat(Math.max(0, 7 - value.length()));
            rgb = SLMathUtils.tryPraseToHex(paddedValue);
        }
        
        if (rgb == null) {
            this.syncHexValue();
            return false;
        }
        
        this.applyRgb(rgb, true);
        this.syncHexValue();
        return true;
    }
    
    ///
    /// Returns the normalized current value of the selected RGB channel.
    ///
    /// @param channel selected RGB channel
    ///
    /// @return normalized channel value
    ///
    public float channelValue(ColorPickerChannel channel) {
        int rgb = this.rgb();
        return switch (channel) {
            case RED -> SLColorUtils.red(rgb) / 255.0F;
            case GREEN -> SLColorUtils.green(rgb) / 255.0F;
            case BLUE -> SLColorUtils.blue(rgb) / 255.0F;
        };
    }
    
    ///
    /// Returns the gradient start color for the selected RGB channel.
    ///
    /// @param channel selected RGB channel
    ///
    /// @return packed ARGB start color
    ///
    public int channelStartColor(ColorPickerChannel channel) {
        int rgb = this.rgb();
        return switch (channel) {
            case RED -> SLColorUtils.argb(0, SLColorUtils.green(rgb), SLColorUtils.blue(rgb));
            case GREEN -> SLColorUtils.argb(SLColorUtils.red(rgb), 0, SLColorUtils.blue(rgb));
            case BLUE -> SLColorUtils.argb(SLColorUtils.red(rgb), SLColorUtils.green(rgb), 0);
        };
    }
    
    ///
    /// Returns the gradient end color for the selected RGB channel.
    ///
    /// @param channel selected RGB channel
    ///
    /// @return packed ARGB end color
    ///
    public int channelEndColor(ColorPickerChannel channel) {
        int rgb = this.rgb();
        return switch (channel) {
            case RED -> SLColorUtils.argb(255, SLColorUtils.green(rgb), SLColorUtils.blue(rgb));
            case GREEN -> SLColorUtils.argb(SLColorUtils.red(rgb), 255, SLColorUtils.blue(rgb));
            case BLUE -> SLColorUtils.argb(SLColorUtils.red(rgb), SLColorUtils.green(rgb), 255);
        };
    }
    
    ///
    /// Synchronizes HSV values from the current RGB value.
    ///
    /// @param rgb     source RGB value
    /// @param syncHue whether grayscale updates are allowed to override hue
    ///
    private void syncHsvFromRgb(int rgb, boolean syncHue) {
        float[] hsv = SLColorUtils.rgbToHsv(rgb);
        this.saturation = hsv[1];
        this.value = hsv[2];
        
        if (!syncHue) return;
        if (hsv[1] <= 0.0F) return;
        if (hsv[0] == 0.0F && this.hue >= 1.0F) return;
        this.hue = hsv[0];
    }
    
    ///
    /// Pushes the current packed RGB value into the display-hex state.
    ///
    private void syncHexValue() {
        this.hexValue.set(SLMathUtils.rgbToHex(this.rgb()));
    }
}
