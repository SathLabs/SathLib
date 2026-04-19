package dev.satherov.sathlib.client.lang;

import lombok.Getter;
import lombok.experimental.Accessors;

import dev.satherov.sathlib.SathLib;

import net.minecraft.util.Util;

import java.util.function.BiConsumer;

///
/// Lang entries used generically
///
@Getter
@Accessors(fluent = true)
public enum GenericLang implements SLTranslatable {
    // @formatter:off
    /// Generic "on" label.
    ON      ("on",       "On"),
    /// Generic "off" label.
    OFF     ("off",      "Off"),
    /// Generic "enabled" label.
    ENABLED ("enabled",  "Enabled"),
    /// Generic "disabled" label.
    DISABLED("disabled", "Disabled"),
    /// Generic "allow" label.
    ALLOW   ("allow",    "Allow"),
    /// Generic "deny" label.
    DENY    ("deny",     "Deny"),
    /// Generic "allowed" label.
    ALLOWED ("allowed",  "Allowed"),
    /// Generic "denied" label.
    DENIED  ("denied",   "Denied"),
    /// Generic "none" label.
    NONE    ("none",     "None"),
    /// Generic "all" label.
    ALL     ("all",      "All"),
    /// Generic "any" label.
    ANY     ("any",      "Any"),
    // @formatter:on
    ;
    
    private final String key;
    private final String translation;
    
    ///
    /// Creates a new lang entry in the `generic` category
    ///
    /// @param key         the translation key path, combined with the category and namespace
    /// @param translation the English translation of this entry
    ///
    GenericLang(String key, String translation) {
        this.key = Util.makeDescriptionId("generic", SathLib.id(key));
        this.translation = translation;
    }
    
    ///
    /// Translates all lang entries of this enum. Should be called in the Language provider
    ///
    /// @param consumer the key-value consumer for the translations
    ///
    public static void translate(BiConsumer<String, String> consumer) {
        for (SLTranslatable lang : GenericLang.values()) {
            consumer.accept(lang.key(), lang.translation());
        }
    }
}
