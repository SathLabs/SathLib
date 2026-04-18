package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import org.jspecify.annotations.Nullable;

///
/// Number and string helpers used by screen widgets.
///
@UtilityClass
public class SLNumberUtils {
    
    ///
    /// Sanitizes the given text input into only valid hex digits
    ///
    /// @param text the text to sanitize
    ///
    /// @return The sanitized String
    ///
    public String sanitizeHex(@Nullable String text) {
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
    
    public @Nullable Integer tryPraseToHex(String text) {
        String hex = SLNumberUtils.sanitizeHex(text);
        if (hex.length() != 6) return null;
        
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
    
    public static String rgbToHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }
    
    public static String argbToHex(int argb) {
        return String.format("#%08X", argb);
    }
}
