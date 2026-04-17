package dev.satherov.sathlib.common.properties;

import dev.satherov.sathlib.network.chat.SLComponent;

import org.jspecify.annotations.Nullable;

///
/// @param value   the property value
/// @param display the display component for the current value
/// @param tooltip the tooltip component for the current value
///
public record SLPropertyValue<T>(@Nullable T value, @Nullable SLComponent display, @Nullable SLComponent tooltip) {
    
    public static <T> SLPropertyValue<T> of(T value) {
        return new SLPropertyValue<>(value, null, null);
    }
    
    public static <T> SLPropertyValue<T> empty() {
        return new SLPropertyValue<>(null, null, null);
    }
    
    public boolean isEmpty() {
        return this.value == null;
    }
}
