package dev.satherov.sathlib.client.lang;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import dev.satherov.sathlib.SathLib;

import net.minecraft.util.Util;

import java.util.function.BiConsumer;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum SLLanguage implements SLTranslatable {
    // @formatter:off
    GENERIC_ON            ("generic", "on",              "On"),
    GENERIC_OFF           ("generic", "off",             "Off"),
    GENERIC_ENABLED       ("generic", "enabled",         "Enabled"),
    GENERIC_DISABLED      ("generic", "disabled",        "Disabled"),
    GENERIC_ALLOW         ("generic", "allow",           "Allow"),
    GENERIC_DENY          ("generic", "deny",            "Deny"),
    GENERIC_NONE          ("generic", "none",            "None"),
    GENERIC_ALL           ("generic", "all",             "All"),
    GENERIC_ANY           ("generic", "any",             "Any"),
                                                         
    INPUT_WHEEL_UP        ("input",   "wheel.up",        "Wheel Up"),
    INPUT_WHEEL_DOWN      ("input",   "wheel.down",      "Wheel Down"),
    
    FORMAT_ROUND_BRACKETS ("format",  "round_brackets",  "(%s)"),
    FORMAT_SQUARE_BRACKETS("format",  "square_brackets", "[%s]"),
    FORMAT_CURLY_BRACKETS ("format",  "curly_brackets",  "{%s}"),
    // @formatter:on
    ;
    
    private final String key;
    private final String translation;
    
    SLLanguage(String group, String key, String translation) {
        this(Util.makeDescriptionId(group, SathLib.id(key)), translation);
    }
    
    public static void translate(BiConsumer<String, String> consumer) {
        for (SLTranslatable lang : SLLanguage.values()) {
            consumer.accept(lang.key(), lang.translation());
        }
    }
}
