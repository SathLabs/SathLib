package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import net.neoforged.fml.loading.FMLEnvironment;

import net.minecraft.client.Minecraft;

import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

///
/// General utils for Strings
///
@UtilityClass
public class SLStringUtils {
    
    private static final Locale LOCALE = Locale.ROOT;
    private static final String[] POSITIVE_PREFIXES = { "", "k", "M", "G", "T", "P", "E" };
    private static final String[] NEGATIVE_PREFIXES = { "", "m", "µ", "n", "p", "f", "a" };
    
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
    /// Formats a `double` as either a plain decimal string or scientific notation.
    /// Plain decimal format is used when the value fits within the requested precision;
    /// otherwise scientific notation is used.
    ///
    /// @param value     the number to format
    /// @param precision the maximum number of significant digits to keep
    ///
    /// @return the formatted number
    ///
    public static String scientific(double value, int precision) {
        if (Double.isNaN(value)) return "NaN";
        if (Double.isInfinite(value)) return value > 0 ? "∞" : "-∞";
        
        final BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        if (decimal.signum() == 0) return "0";
        
        final BigDecimal abs = decimal.abs();
        final int digits = abs.precision();
        
        if (digits <= precision) return decimal.toPlainString();
        
        final int exp = abs.precision() - abs.scale() - 1;
        BigDecimal mantissa = decimal.movePointLeft(exp)
                .setScale(Math.max(precision - 1, 0), RoundingMode.DOWN)
                .stripTrailingZeros();
        
        if (mantissa.scale() <= 0) mantissa = mantissa.setScale(1, RoundingMode.DOWN);
        
        final StringBuilder builder = new StringBuilder();
        builder.append(mantissa.toPlainString());
        builder.append("e");
        
        if (exp >= 0) builder.append("+");
        
        builder.append(exp);
        return builder.toString();
    }
    
    ///
    /// Displays the given `double` using the current system locale.
    ///
    /// Examples:
    /// - `Locale.US` -> `1,234.5`
    /// - `Locale.GERMANY` -> `1.234,5`
    ///
    /// @param value Number to display.
    ///
    /// @return Locale-formatted decimal string.
    ///
    public static String displayDecimal(double value) {
        final NumberFormat formatter = NumberFormat.getNumberInstance(FMLEnvironment.getDist().isClient() ? Minecraft.getInstance().getLocale() : Locale.getDefault());
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(16);
        formatter.setMinimumFractionDigits(0);
        
        return formatter.format(BigDecimal.valueOf(value));
    }
    
    ///
    /// Displays the given `float` using the current system locale.
    ///
    /// @param value Number to display.
    ///
    /// @return Locale-formatted decimal string.
    ///
    public static String displayDecimal(float value) {
        return SLStringUtils.displayDecimal((double) value);
    }
    
    ///
    /// Displays the given `double` using the current system locale and appends an SI prefix.
    ///
    /// The number is scaled in steps of `1000`.
    ///
    /// Examples:
    /// - `1500` -> `1.5k`
    /// - `1200000` -> `1.2M`
    /// - `0.0012` -> `1.2m`
    /// - `0.0000012` -> `1.2µ`
    ///
    /// @param value Number to display.
    ///
    /// @return Locale-formatted number with an SI prefix.
    ///
    public static String displaySi(double value) {
        return SLStringUtils.displaySi(value, false);
    }
    
    ///
    /// Displays the given `double` using the current system locale and appends an SI prefix.
    ///
    /// The number is scaled in steps of `1000`.
    ///
    /// Examples:
    /// - `1500` -> `1.5k`
    /// - `1200000` -> `1.2M`
    /// - `0.0012` -> `1.2m`
    /// - `0.0000012` -> `1.2µ`
    ///
    /// @param value           Number to display.
    /// @param spaceBeforeUnit Whether to add a space between the number and the SI prefix.
    ///
    /// @return Locale-formatted number with an SI prefix.
    ///
    public static String displaySi(double value, boolean spaceBeforeUnit) {
        if (Double.isNaN(value)) return "NaN";
        if (Double.isInfinite(value)) return value > 0 ? "∞" : "-∞";
        if (value == 0.0D) return "0";
        
        double scaledValue = value;
        double absValue = Math.abs(value);
        int prefixIndex = 0;
        String[] prefixes = SLStringUtils.POSITIVE_PREFIXES;
        
        if (absValue >= 1.0D) {
            while (absValue >= 1000.0D && prefixIndex < SLStringUtils.POSITIVE_PREFIXES.length - 1) {
                scaledValue /= 1000.0D;
                absValue /= 1000.0D;
                prefixIndex++;
            }
        } else {
            prefixes = SLStringUtils.NEGATIVE_PREFIXES;
            
            while (absValue < 1.0D && prefixIndex < SLStringUtils.NEGATIVE_PREFIXES.length - 1) {
                scaledValue *= 1000.0D;
                absValue *= 1000.0D;
                prefixIndex++;
            }
        }
        
        String number = SLStringUtils.displayDecimal(scaledValue);
        String prefix = prefixes[prefixIndex];
        
        if (prefix.isEmpty()) return number;
        if (spaceBeforeUnit) return number + " " + prefix;
        
        return number + prefix;
    }
    
    ///
    /// Displays the given `float` using the current system locale and appends an SI prefix.
    ///
    /// @param value Number to display.
    ///
    /// @return Locale-formatted number with an SI prefix.
    ///
    public static String displaySi(float value) {
        return SLStringUtils.displaySi((double) value, false);
    }
    
    ///
    /// Displays the given `float` using the current system locale and appends an SI prefix.
    ///
    /// @param value           Number to display.
    /// @param spaceBeforeUnit Whether to add a space between the number and the SI prefix.
    ///
    /// @return Locale-formatted number with an SI prefix.
    ///
    public static String displaySi(float value, boolean spaceBeforeUnit) {
        return SLStringUtils.displaySi((double) value, spaceBeforeUnit);
    }
    
    ///
    /// Splits the given string into words.
    ///
    /// @param input String to split.
    ///
    /// @return List of words.
    ///
    @VisibleForTesting
    static List<String> words(@Nullable String input) {
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
