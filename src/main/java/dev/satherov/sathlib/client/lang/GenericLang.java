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
@SuppressWarnings("doclint:missing")
public enum GenericLang implements SLTranslatable {
    // @formatter:off
    ON         ("on",          "On"),
    OFF        ("off",         "Off"),
    ENABLED    ("enabled",     "Enabled"),
    DISABLED   ("disabled",    "Disabled"),
    ALLOW      ("allow",       "Allow"),
    DENY       ("deny",        "Deny"),
    ALLOWED    ("allowed",     "Allowed"),
    DENIED     ("denied",      "Denied"),
    NONE       ("none",        "None"),
    ALL        ("all",         "All"),
    ANY        ("any",         "Any"),
    SUPPORTED  ("supported",   "Supported"),
    UNSUPPORTED("unsupported", "Unsupported"),
    EMPTY      ("empty",       "Empty"),
    FULL       ("full",        "Full"),
    FILLED     ("filled",      "Filled"),
    ENERGY     ("energy",      "Energy"),
    FLUID      ("fluid",       "Fluid"),
    ITEM       ("item",        "Item"),
    ITEMS      ("items",       "Items"),
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
