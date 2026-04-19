package dev.satherov.sathlib.util;

import org.jspecify.annotations.Nullable;

///
/// Number and string helpers used by screen widgets.
///
public class SLNumberUtils {
    
    private SLNumberUtils() { }
    
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
        String hex = SLNumberUtils.sanitizeHex(text);
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
