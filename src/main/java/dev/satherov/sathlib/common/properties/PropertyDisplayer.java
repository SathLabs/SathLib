package dev.satherov.sathlib.common.properties;

import dev.satherov.sathlib.client.lang.SLDisplayable;
import dev.satherov.sathlib.client.lang.SLTranslatable;
import dev.satherov.sathlib.network.chat.SLComponent;

///
/// Displays a {@link SLComponent} based on the supplied value.
///
/// @param <T> The type of the value to display
///
/// @see PropertyEnum
/// @see BlockItemProperty
///
@FunctionalInterface
public interface PropertyDisplayer<T> {
    
    ///
    /// Displays the value as a {@link SLComponent}.
    ///
    SLComponent display(T value);
    
    ///
    /// Displays a {@link SLComponent} directly, ignoring the value.
    ///
    /// @param component The component to display
    /// @param <T>       The type of the value to display
    ///
    /// @return the property displayer
    ///
    static <T> PropertyDisplayer<T> direct(SLComponent component) {
        return _ -> component;
    }
    
    ///
    /// Displays a {@link SLTranslatable} directly, ignoring the value.
    ///
    /// @param translatable The translatable to display
    /// @param <T>          The type of the value to display
    ///
    /// @return the property displayer
    ///
    static <T> PropertyDisplayer<T> direct(SLTranslatable translatable) {
        return _ -> translatable.translate();
    }
    
    ///
    /// Displays a {@link SLComponent} based on a boolean value. `on` if `true`, `off` otherwise.
    ///
    /// @param on  The component to display if the value is `true`
    /// @param off The component to display if the value is `false`
    ///
    /// @return the property displayer
    ///
    static PropertyDisplayer<Boolean> boolDisplayer(SLComponent on, SLComponent off) {
        return value -> value ? on : off;
    }
    
    ///
    /// Displays a {@link SLTranslatable} based on a boolean value. `on` if `true`, `off` otherwise.
    ///
    /// @param on  The translatable to display if the value is `true`
    /// @param off The translatable to display if the value is `false`
    ///
    static PropertyDisplayer<Boolean> boolDisplayer(SLTranslatable on, SLTranslatable off) {
        return value -> value ? on.translate() : off.translate();
    }
    
    ///
    /// Displays the value of an {@link PropertyEnum} as a {@link SLComponent}.
    ///
    /// @param <T> The type of the enum
    ///
    /// @return the property displayer
    ///
    static <T extends Enum<T> & PropertyEnum> PropertyDisplayer<T> enumValueDisplayer() {
        return PropertyEnum::display;
    }
    
    ///
    /// Displays the tooltip of an {@link PropertyEnum} as a {@link SLComponent}.
    ///
    /// @param <T> The type of the enum
    ///
    /// @return the property displayer
    ///
    static <T extends Enum<T> & PropertyEnum> PropertyDisplayer<T> enumTooltipDisplayer() {
        return PropertyEnum::tooltip;
    }
    
    ///
    /// Displays the default value of a property as a {@link SLComponent}.
    ///
    /// @param <T> The type of the value
    ///
    /// @return the property displayer
    ///
    static <T> PropertyDisplayer<T> defaultValueDisplayer() {
        return val -> switch (val) {
            case String string -> SLComponent.string(string);
            case Boolean bool -> SLComponent.enabledDisabled(bool);
            case SLDisplayable displayable -> displayable.display();
            case null -> SLComponent.empty();
            default -> SLComponent.string(String.valueOf(val));
        };
    }
    
    ///
    /// Returns an empty property displayer.
    ///
    /// @param <T> The type of the value
    ///
    /// @return the property displayer
    ///
    static <T> PropertyDisplayer<T> empty() {
        return _ -> SLComponent.empty();
    }
}
