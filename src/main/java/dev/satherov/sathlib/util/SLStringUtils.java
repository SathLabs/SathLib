package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import org.intellij.lang.annotations.PrintFormat;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

///
/// General utils for Strings
///
@UtilityClass
public class SLStringUtils {
    
    private static final Locale LOCALE = Locale.ROOT;
    
    ///
    /// Converts the given string to lowercase.
    ///
    /// @param string String to convert.
    ///
    /// @return Lowercased string with {@link Locale#ROOT} applied.
    ///
    public static String lower(String string) {
        return string.toLowerCase(SLStringUtils.LOCALE);
    }
    
    ///
    /// Converts the given object to a lower case string
    ///
    /// @param object Object to convert
    ///
    /// @return Lowercased string with {@link Locale#ROOT} applied.
    ///
    public static String lower(Object object) {
        return SLStringUtils.lower(String.valueOf(object));
    }
    
    ///
    /// Converts the given string to uppercase.
    ///
    /// @param string String to convert.
    ///
    /// @return Uppercased string with {@link Locale#ROOT} applied.
    ///
    public static String upper(String string) {
        return string.toUpperCase(SLStringUtils.LOCALE);
    }
    
    ///
    /// Converts the given object to uppercase.
    ///
    /// @param object Object to convert.
    ///
    /// @return Uppercased string with {@link Locale#ROOT} applied.
    ///
    public static String upper(Object object) {
        return SLStringUtils.upper(String.valueOf(object));
    }
    
    ///
    /// Formats the given string using the given arguments.
    ///
    /// @param string String to format.
    /// @param args   Arguments to format the string with.
    ///
    /// @return Formatted string with {@link Locale#ROOT} applied
    ///
    public static String format(@PrintFormat String string, Object... args) {
        return String.format(SLStringUtils.LOCALE, string, args);
    }
    
    ///
    /// Converts the given string to camel case.
    ///
    /// `hello-world_thisIs  aTEST` -> `helloWorldThisIsATest`
    ///
    /// @param input String to convert.
    ///
    /// @return Camel cased string.
    ///
    public static String toCamelCase(String input) {
        List<String> words = SLStringUtils.words(input);
        if (words.isEmpty()) return "";
        
        StringBuilder result = new StringBuilder();
        result.append(words.getFirst());
        
        for (int index = 1; index < words.size(); index++) {
            String word = words.get(index);
            result.append(SLStringUtils.capitalize(word));
        }
        
        return result.toString();
    }
    
    ///
    /// Converts the given string to pascal case.
    ///
    /// `hello-world_thisIs  aTEST` -> `HelloWorldThisIsATest`
    ///
    /// @param input String to convert.
    ///
    /// @return Pascal cased string.
    ///
    public static String toPascalCase(String input) {
        List<String> words = SLStringUtils.words(input);
        if (words.isEmpty()) return "";
        
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(SLStringUtils.capitalize(word));
        }
        
        return result.toString();
    }
    
    ///
    /// Converts the given string to snake case.
    ///
    /// `hello-world_thisIs  aTEST` -> `hello_world_this_is_a_test`
    ///
    /// @param input String to convert.
    ///
    /// @return Snake cased string.
    ///
    public static String toSnakeCase(String input) {
        List<String> words = SLStringUtils.words(input);
        return String.join("_", words);
    }
    
    ///
    /// Converts the given string to screaming snake case.
    ///
    /// `hello-world_thisIs  aTEST` -> `HELLO_WORLD_THIS_IS_A_TEST`
    ///
    /// @param input String to convert.
    ///
    /// @return Screaming snake case string.
    ///
    public static String toScreamingSnakeCase(String input) {
        List<String> words = SLStringUtils.words(input);
        
        String result = String.join("_", words);
        return result.toUpperCase(SLStringUtils.LOCALE);
    }
    
    ///
    /// Converts the given string to kebab case.
    ///
    /// `hello-world_thisIs  aTEST` -> `hello-world-this-is-a-test`
    ///
    /// @param input String to convert.
    ///
    /// @return Kebab cased string.
    ///
    public static String toKebabCase(String input) {
        List<String> words = SLStringUtils.words(input);
        return String.join("-", words);
    }
    
    ///
    /// Converts the given string to sentence case.
    ///
    /// `hello-world_thisIs  aTEST` -> `Hello world this is a test`
    ///
    /// @param input String to convert.
    ///
    /// @return Sentence cased string.
    ///
    public static String toSentenceCase(String input) {
        List<String> words = SLStringUtils.words(input);
        if (words.isEmpty()) return "";
        
        words.set(0, SLStringUtils.capitalize(words.getFirst()));
        return String.join(" ", words);
    }
    
    ///
    /// Converts the given string to title case.
    ///
    /// `hello-world_thisIs  aTEST` -> `Hello World This Is A Test`
    ///
    /// @param input String to convert.
    ///
    /// @return Title cased string.
    ///
    public static String toTitleCase(String input) {
        List<String> words = SLStringUtils.words(input);
        if (words.isEmpty()) return "";
        
        words.replaceAll(SLStringUtils::capitalize);
        return String.join(" ", words);
    }
    
    ///
    /// Capitalizes the first letter of the given word and turns the rest into lowercase.
    ///
    /// `eXaMpLe` -> `Example`
    ///
    /// @param word Word to capitalize.
    ///
    /// @return Capitalized word.
    ///
    public static String capitalize(@Nullable String word) {
        if (word == null || word.isEmpty()) return "";
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(SLStringUtils.LOCALE);
    }
    
    ///
    /// Formats a floating point number with the given precision.
    ///
    /// @param value     Floating point number to format.
    /// @param precision Number of digits after the decimal point.
    ///
    /// @return Formatted decimal string.
    ///
    public static String decimal(double value, int precision) {
        final String base = String.valueOf(value);
        if (base.length() - 1 <= precision) return base;
        final String format = "%." + precision + "f";
        return String.format(Locale.ROOT, format, value);
    }
    
    ///
    /// Formats a floating point number with the given precision in scientific notation.
    ///
    /// @param value     Floating point number to format.
    /// @param precision Number of digits after the decimal point.
    ///
    /// @return Formatted decimal string in scientific notation.
    ///
    public static String scientific(double value, int precision) {
        final String base = String.valueOf(value);
        if (base.length() - 1 <= precision) return base;
        final String format = "%." + precision + "e";
        return String.format(Locale.ROOT, format, value);
    }
    
    ///
    /// Splits the given string into words.
    ///
    /// @param input String to split.
    ///
    /// @return List of words.
    ///
    private static List<String> words(@Nullable String input) {
        if (input == null || input.isBlank()) return new ArrayList<>(0);
        
        String[] parts = input
                .replaceAll("[_\\-]+", " ")
                .replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("[^A-Za-z0-9 ]+", " ")
                .trim()
                .split("\\s+");
        
        List<String> words = new ArrayList<>(parts.length);
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            words.add(part.toLowerCase(SLStringUtils.LOCALE));
        }
        
        return words;
    }
}
