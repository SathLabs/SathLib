package dev.satherov.sathlib.client.lang;

import lombok.Getter;
import lombok.experimental.Accessors;

import dev.satherov.sathlib.SathLib;

import net.minecraft.util.Util;

import java.util.function.BiConsumer;

///
/// Lang entries used purely for formatting
///
@Getter
@Accessors(fluent = true)
@SuppressWarnings("doclint:missing")
public enum FormattingLang implements SLTranslatable {
    // @formatter:off
    ROUND_BRACKETS ("round_brackets",  "(%s)"),
    SQUARE_BRACKETS("square_brackets", "[%s]"),
    CURLY_BRACKETS ("curly_brackets",  "{%s}"),
    // @formatter:on
    ;
    
    private final String key;
    private final String translation;
    
    ///
    /// Creates a new lang entry in the `formatting` category
    ///
    /// @param key         the translation key path, combined with the category and namespace
    /// @param translation the English translation of this entry
    ///
    FormattingLang(String key, String translation) {
        this.key = Util.makeDescriptionId("formatting", SathLib.id(key));
        this.translation = translation;
    }
    
    ///
    /// Translates all lang entries of this enum. Should be called in the Language provider
    ///
    /// @param consumer the key-value consumer for the translations
    ///
    public static void translate(BiConsumer<String, String> consumer) {
        for (SLTranslatable lang : FormattingLang.values()) {
            consumer.accept(lang.key(), lang.translation());
        }
    }
}
