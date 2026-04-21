package dev.satherov.sathlib.client.lang;

import lombok.Getter;
import lombok.experimental.Accessors;

import dev.satherov.sathlib.SathLib;

import net.minecraft.util.Util;

import java.util.function.BiConsumer;

///
/// Lang entries for input values
///
@Getter
@Accessors(fluent = true)
@SuppressWarnings("doclint:missing")
public enum InputLang implements SLTranslatable {
    // @formatter:off
    WHEEL_UP  ("wheel.up",   "Wheel Up"),
    WHEEL_DOWN("wheel.down", "Wheel Down"),
    // @formatter:on
    ;
    
    private final String key;
    private final String translation;
    
    ///
    /// Creates a new lang entry in the `input` category
    ///
    /// @param key         the translation key path, combined with the category and namespace
    /// @param translation the English translation of this entry
    ///
    InputLang(String key, String translation) {
        this.key = Util.makeDescriptionId("input", SathLib.id(key));
        this.translation = translation;
    }
    
    ///
    /// Translates all lang entries of this enum. Should be called in the Language provider
    ///
    /// @param consumer the key-value consumer for the translations
    ///
    public static void translate(BiConsumer<String, String> consumer) {
        for (SLTranslatable lang : InputLang.values()) {
            consumer.accept(lang.key(), lang.translation());
        }
    }
}
