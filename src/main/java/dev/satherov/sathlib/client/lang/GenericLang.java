package dev.satherov.sathlib.client.lang;

import lombok.Getter;
import lombok.experimental.Accessors;

import dev.satherov.sathlib.SathLib;

import net.minecraft.util.Util;

import java.util.function.BiConsumer;

@Getter
@Accessors(fluent = true)
public enum GenericLang implements SLTranslatable {
    // @formatter:off
    ON      ("on",       "On"),
    OFF     ("off",      "Off"),
    ENABLED ("enabled",  "Enabled"),
    DISABLED("disabled", "Disabled"),
    ALLOW   ("allow",    "Allow"),
    DENY    ("deny",     "Deny"),
    ALLOWED ("allowed",  "Allowed"),
    DENIED  ("denied",   "Denied"),
    NONE    ("none",     "None"),
    ALL     ("all",      "All"),
    ANY     ("any",      "Any"),
    // @formatter:on
    ;
    
    private final String key;
    private final String translation;
    
    
    GenericLang(String key, String translation) {
        this.key = Util.makeDescriptionId("generic", SathLib.id(key));
        this.translation = translation;
    }
    
    public static void translate(BiConsumer<String, String> consumer) {
        for (SLTranslatable lang : GenericLang.values()) {
            consumer.accept(lang.key(), lang.translation());
        }
    }
}
