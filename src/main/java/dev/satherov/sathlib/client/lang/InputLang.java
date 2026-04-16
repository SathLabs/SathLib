package dev.satherov.sathlib.client.lang;

import lombok.Getter;
import lombok.experimental.Accessors;

import dev.satherov.sathlib.SathLib;

import net.minecraft.util.Util;

import java.util.function.BiConsumer;

@Getter
@Accessors(fluent = true)
public enum InputLang implements SLTranslatable {
    // @formatter:off
    WHEEL_UP  ("wheel.up",   "Wheel Up"),
    WHEEL_DOWN("wheel.down", "Wheel Down"),
    // @formatter:on
    ;
    
    private final String key;
    private final String translation;
    
    InputLang(String key, String translation) {
        this.key = Util.makeDescriptionId("input", SathLib.id(key));
        this.translation = translation;
    }
    
    public static void translate(BiConsumer<String, String> consumer) {
        for (SLTranslatable lang : InputLang.values()) {
            consumer.accept(lang.key(), lang.translation());
        }
    }
}
