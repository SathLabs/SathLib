package dev.satherov.sathlib.client.lang;

import lombok.Getter;
import lombok.experimental.Accessors;

import dev.satherov.sathlib.SathLib;

import net.minecraft.util.Util;

import java.util.function.BiConsumer;

@Getter
@Accessors(fluent = true)
public enum FormattingLang implements SLTranslatable {
    // @formatter:off
    ROUND_BRACKETS ("round_brackets",  "(%s)"),
    SQUARE_BRACKETS("square_brackets", "[%s]"),
    CURLY_BRACKETS ("curly_brackets",  "{%s}"),
    // @formatter:on
    ;
    
    private final String key;
    private final String translation;
    
    FormattingLang(String key, String translation) {
        this.key = Util.makeDescriptionId("input", SathLib.id(key));
        this.translation = translation;
    }
    
    public static void translate(BiConsumer<String, String> consumer) {
        for (SLTranslatable lang : FormattingLang.values()) {
            consumer.accept(lang.key(), lang.translation());
        }
    }
}
