package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import net.minecraft.util.Mth;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

///
/// Utility class for math operations.
///
@UtilityClass
public class SLMathUtils {
    
    ///
    /// Gets the cosinus by degrees. Use {@link Math#cos(double)} for radian
    ///
    /// @param degree Degree to convert
    ///
    /// @return Cosinus of from the degree
    ///
    public static double cos(double degree) {
        return StrictMath.cos(degree * Mth.DEG_TO_RAD);
    }
    
    ///
    /// Gets the cosinus by degrees. Use {@link Math#cos(double)} for radian
    ///
    /// @param degree Degree to convert
    ///
    /// @return Cosinus of from the degree
    ///
    public static float cos(float degree) {
        return (float) StrictMath.cos(degree * Mth.DEG_TO_RAD);
    }
    
    ///
    /// Gets the sinus by degrees. Use {@link Math#sin(double)} for radian
    ///
    /// @param degree Degree to convert
    ///
    /// @return Sinus of from the degree
    ///
    public static double sin(double degree) {
        return StrictMath.sin(degree * Mth.DEG_TO_RAD);
    }
    
    ///
    /// Gets the sinus by degrees. Use {@link Math#sin(double)} for radian
    ///
    /// @param degree Degree to convert
    ///
    /// @return Sinus of from the degree
    ///
    public static float sin(float degree) {
        return (float) StrictMath.sin(degree * Mth.DEG_TO_RAD);
    }
    
    ///
    /// Gets a vector at a given angle and distance from the center. The new vector is located by drawing a line
    /// from the center `center` with the given length `radius` at the provided angle `degrees`
    ///
    /// @param center  Center point
    /// @param degrees angle in degrees
    /// @param radius  distance from the center
    ///
    /// @return Vector located at the calculated offset
    ///
    public static Vector2i getPointOnCircle(Vector2i center, float degrees, float radius) {
        return new Vector2i(
                (int) (center.x() + (SLMathUtils.cos(degrees) * radius)),
                (int) (center.y() + (SLMathUtils.sin(degrees) * radius))
        );
    }
    
    ///
    /// Gets a vector at a given angle and distance from the center. The new vector is located by drawing a line
    /// from the center `center` with the given length `radius` at the provided angle `degrees`
    ///
    /// @param center  Center point
    /// @param degrees angle in degrees
    /// @param radius  distance from the center
    ///
    /// @return Vector located at the calculated offset
    ///
    public static Vector2f getPointOnCircle(Vector2f center, float degrees, float radius) {
        return new Vector2f(
                center.x() + (SLMathUtils.cos(degrees) * radius),
                center.y() + (SLMathUtils.sin(degrees) * radius)
        );
    }
    
    ///
    /// Gets a vector at a given angle and distance from the center. The new vector is located by drawing a line
    /// from the center `center` with the given length `radius` at the provided angle `degrees`
    ///
    /// @param center  Center point
    /// @param degrees angle in degrees
    /// @param radius  distance from the center
    ///
    /// @return Vector located at the calculated offset
    ///
    public static Vector2d getPointOnCircle(Vector2d center, float degrees, float radius) {
        return new Vector2d(
                center.x() + (SLMathUtils.cos(degrees) * radius),
                center.y() + (SLMathUtils.sin(degrees) * radius)
        );
    }
    
    ///
    /// Splits the given value into the given amount of near-even parts.
    ///
    /// @param value Value to split.
    /// @param parts Amount of parts to create.
    ///
    /// @return Array containing the split parts.
    ///
    public static int[] split(int value, int parts) {
        if (parts <= 0) return new int[0];
        
        final int[] result = new int[parts];
        final int base = value / parts;
        final int remainder = value % parts;
        for (int index = 0; index < parts; index++) {
            result[index] = base + (index < remainder ? 1 : 0);
        }
        return result;
    }
    
    ///
    /// Sanitizes the given text input into only valid hex digits
    ///
    /// @param text the text to sanitize
    ///
    /// @return The sanitized String
    ///
    public static String sanitizeHex(@Nullable String text) {
        if (text == null || text.isBlank()) return "";
        
        String source = text.trim();
        StringBuilder digits = new StringBuilder(6);
        
        for (int index = 0; index < source.length(); index++) {
            char character = source.charAt(index);
            if (character == '#') continue;
            if (Character.digit(character, 16) >= 0) digits.append(Character.toUpperCase(character));
            if (digits.length() == 6) break;
        }
        
        return digits.toString();
    }
    
    ///
    /// Attempts to parse an RGB hex string after sanitizing it.
    ///
    /// @param text source text to parse
    ///
    /// @return parsed RGB value, or {@code null} when parsing fails
    ///
    public static @Nullable Integer tryPraseToHex(String text) {
        String hex = SLMathUtils.sanitizeHex(text);
        if (hex.length() != 6) return null;
        
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
    
    ///
    /// Formats an RGB color as a six-digit hex string.
    ///
    /// @param rgb packed RGB color
    ///
    /// @return formatted hex string prefixed with {@code #}
    ///
    public static String rgbToHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }
    
    ///
    /// Formats an ARGB color as an eight-digit hex string.
    ///
    /// @param argb packed ARGB color
    ///
    /// @return formatted hex string prefixed with {@code #}
    ///
    public static String argbToHex(int argb) {
        return String.format("#%08X", argb);
    }
}
