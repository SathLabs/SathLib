package dev.satherov.sathlib.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import net.minecraft.util.Mth;

import java.util.function.UnaryOperator;

///
/// Utility class for handling colors.
///
@UtilityClass
public class SLColorUtils {
    
    ///
    /// Extracts the alpha channel from a packed ARGB color.
    ///
    /// @param color the packed color
    ///
    /// @return the alpha value
    ///
    public static int alpha(int color) {
        return color >>> 24;
    }
    
    ///
    /// Extracts the red channel from a packed ARGB or RGB color.
    ///
    /// @param color the packed color
    ///
    /// @return the red value
    ///
    public static int red(int color) {
        return color >> 16 & 0xFF;
    }
    
    ///
    /// Extracts the green channel from a packed ARGB or RGB color.
    ///
    /// @param color the packed color
    ///
    /// @return the green value
    ///
    public static int green(int color) {
        return color >> 8 & 0xFF;
    }
    
    ///
    /// Extracts the blue channel from a packed ARGB or RGB color.
    ///
    /// @param color the packed color
    ///
    /// @return the blue value
    ///
    public static int blue(int color) {
        return color & 0xFF;
    }
    
    ///
    /// Creates a packed ARGB color from the given channel values.
    ///
    /// @param alpha the alpha value
    /// @param red   the red value
    /// @param green the green value
    /// @param blue  the blue value
    ///
    /// @return the packed ARGB color
    ///
    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
    
    ///
    /// Creates a packed ARGB color from the given RGB channel values with an alpha of `255`.
    ///
    /// @param red   the red value
    /// @param green the green value
    /// @param blue  the blue value
    ///
    /// @return the packed ARGB color
    ///
    public static int argb(int red, int green, int blue) {
        return 0xFF << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
    
    ///
    /// Creates a packed RGB color from the given channel values.
    ///
    /// @param red   the red value
    /// @param green the green value
    /// @param blue  the blue value
    ///
    /// @return the packed RGB color
    ///
    public static int rgb(int red, int green, int blue) {
        return red << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }
    
    ///
    /// Linearly interpolates between two packed ARGB colors.
    ///
    /// @param alpha the interpolation factor in the range `[0, 1]`
    /// @param from  the starting color
    /// @param to    the ending color
    ///
    /// @return the interpolated color
    ///
    public static int lerp(float alpha, int from, int to) {
        final int a = Mth.lerpInt(alpha, SLColorUtils.alpha(from), SLColorUtils.alpha(to));
        final int r = Mth.lerpInt(alpha, SLColorUtils.red(from), SLColorUtils.red(to));
        final int g = Mth.lerpInt(alpha, SLColorUtils.green(from), SLColorUtils.green(to));
        final int b = Mth.lerpInt(alpha, SLColorUtils.blue(from), SLColorUtils.blue(to));
        return SLColorUtils.argb(a, r, g, b);
    }
    
    ///
    /// Replaces the alpha channel of one packed color.
    ///
    /// @param color the packed color
    /// @param alpha the replacement alpha channel
    ///
    /// @return the recolored packed ARGB value
    ///
    public static int withAlpha(int color, int alpha) {
        return SLColorUtils.argb(Mth.clamp(alpha, 0, 255), SLColorUtils.red(color), SLColorUtils.green(color), SLColorUtils.blue(color));
    }
    
    ///
    /// Scales the alpha channel of one packed color.
    ///
    /// @param color  the packed color
    /// @param factor alpha scale factor
    ///
    /// @return the alpha-scaled packed ARGB value
    ///
    public static int multiplyAlpha(int color, float factor) {
        int alpha = Math.clamp(Math.round(SLColorUtils.alpha(color) * factor), 0, 255);
        return SLColorUtils.withAlpha(color, alpha);
    }
    
    ///
    /// Converts an HSV color to a packed RGB color with a fully opaque alpha channel.
    ///
    /// @param hue        the hue component, wrapped to `[0, 1)`
    /// @param saturation the saturation component, clamped to `[0, 1]`
    /// @param value      the value component, clamped to `[0, 1]`
    ///
    /// @return the packed RGB color
    ///
    public static int hsvToRgb(float hue, float saturation, float value) {
        return SLColorUtils.hsvToArgb(hue, saturation, value, 255);
    }
    
    ///
    /// Converts an HSV color to a packed ARGB color.
    ///
    /// @param hue        the hue component, wrapped to `[0, 1)`
    /// @param saturation the saturation component, clamped to `[0, 1]`
    /// @param value      the value component, clamped to `[0, 1]`
    /// @param alpha      the alpha component, clamped to `[0, 255]`
    ///
    /// @return the packed ARGB color
    ///
    public static int hsvToArgb(float hue, float saturation, float value, int alpha) {
        float wrappedHue = SLColorUtils.wrapHue(hue);
        float clampedSat = Mth.clamp(saturation, 0.0F, 1.0F);
        float clampedVal = Mth.clamp(value, 0.0F, 1.0F);
        int clampedAlpha = Mth.clamp(alpha, 0, 255);
        
        if (clampedSat <= 0.0F) {
            int gray = Mth.clamp((int) (clampedVal * 255.0F), 0, 255);
            return SLColorUtils.argb(clampedAlpha, gray, gray, gray);
        }
        
        float scaledHue = wrappedHue * 6.0F;
        int sector = Mth.clamp((int) scaledHue, 0, 5);
        float fraction = scaledHue - (float) sector;
        
        float base = clampedVal * (1.0F - clampedSat);
        float down = clampedVal * (1.0F - clampedSat * fraction);
        float up = clampedVal * (1.0F - clampedSat * (1.0F - fraction));
        
        float red;
        float green;
        float blue;
        
        switch (sector) {
            case 0 -> {
                red = clampedVal;
                green = up;
                blue = base;
            }
            case 1 -> {
                red = down;
                green = clampedVal;
                blue = base;
            }
            case 2 -> {
                red = base;
                green = clampedVal;
                blue = up;
            }
            case 3 -> {
                red = base;
                green = down;
                blue = clampedVal;
            }
            case 4 -> {
                red = up;
                green = base;
                blue = clampedVal;
            }
            case 5 -> {
                red = clampedVal;
                green = base;
                blue = down;
            }
            default -> throw new IllegalStateException("Unreachable sector: " + sector);
        }
        
        int redInt = Mth.clamp((int) (red * 255.0F), 0, 255);
        int greenInt = Mth.clamp((int) (green * 255.0F), 0, 255);
        int blueInt = Mth.clamp((int) (blue * 255.0F), 0, 255);
        return SLColorUtils.argb(clampedAlpha, redInt, greenInt, blueInt);
    }
    
    ///
    /// Wraps a hue value into the normalized range `[0, 1)`.
    ///
    /// @param hue the hue value to wrap
    ///
    /// @return the wrapped hue
    ///
    public static float wrapHue(float hue) {
        float result = hue % 1.0F;
        if (result < 0.0F) result += 1.0F;
        if (result >= 1.0F) result = 0.0F;
        return result;
    }
    
    ///
    /// Converts a packed RGB or ARGB color to HSV components.
    ///
    /// @param rgb the packed color
    ///
    /// @return an array containing hue, saturation, and value in that order
    ///
    public static float[] rgbToHsv(int rgb) {
        float red = SLColorUtils.red(rgb) / 255.0F;
        float green = SLColorUtils.green(rgb) / 255.0F;
        float blue = SLColorUtils.blue(rgb) / 255.0F;
        
        float value = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = value - min;
        
        float hue;
        if (delta == 0.0F) hue = 0.0F;
        else if (value == red) hue = ((green - blue) / delta) % 6.0F;
        else if (value == green) hue = ((blue - red) / delta) + 2.0F;
        else hue = ((red - green) / delta) + 4.0F;
        
        hue /= 6.0F;
        if (hue < 0.0F) hue += 1.0F;
        
        float saturation = value == 0.0F ? 0.0F : delta / value;
        
        return new float[]{
                SLColorUtils.wrapHue(hue),
                Mth.clamp(saturation, 0.0F, 1.0F),
                Mth.clamp(value, 0.0F, 1.0F)
        };
    }
    
    ///
    /// A color channel extractor for packed colors.
    ///
    @RequiredArgsConstructor
    public enum Channel {
        /// Alpha channel extractor.
        ALPHA(SLColorUtils::alpha),
        /// Red channel extractor.
        RED(SLColorUtils::red),
        /// Green channel extractor.
        GREEN(SLColorUtils::green),
        /// Blue channel extractor.
        BLUE(SLColorUtils::blue),
        ;
        
        private final UnaryOperator<Integer> constructor;
        
        ///
        /// Extracts this channel from the given packed color.
        ///
        /// @param packed the packed color
        ///
        /// @return the extracted channel value
        ///
        public int of(int packed) {
            return this.constructor.apply(packed);
        }
    }
}
