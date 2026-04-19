package dev.satherov.sathlib.common.properties;

import dev.satherov.sathlib.network.chat.SLComponent;

import org.jspecify.annotations.Nullable;

///
/// Resolved property value together with optional display metadata.
///
/// @param <T>     logical property value type
/// @param value   the property value
/// @param display the display component for the current value
/// @param tooltip the tooltip component for the current value
///
public record SLPropertyValue<T>(@Nullable T value, @Nullable SLComponent display, @Nullable SLComponent tooltip) {
    
    ///
    /// Creates a property value carrying only a raw value.
    ///
    /// @param value raw property value
    /// @param <T>   logical property value type
    ///
    /// @return property value with no custom display metadata
    ///
    public static <T> SLPropertyValue<T> of(T value) {
        return new SLPropertyValue<>(value, null, null);
    }
    
    ///
    /// Creates an empty property value.
    ///
    /// @param <T> logical property value type
    ///
    /// @return empty property value
    ///
    public static <T> SLPropertyValue<T> empty() {
        return new SLPropertyValue<>(null, null, null);
    }
    
    ///
    /// Returns whether no raw value is currently available.
    ///
    /// @return {@code true} when the record does not carry a value
    ///
    public boolean isEmpty() {
        return this.value == null;
    }
}
