package dev.satherov.sathlib.config.data;

import dev.satherov.sathlib.util.SLStringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// Marks a field as a config value. Must use a specific type to be valid
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigEntry {
    
    ///
    /// Config key of the value.
    /// By default, the field name will be used with {@link SLStringUtils#toSnakeCase(String)}
    ///
    /// @return Config key of the value.
    ///
    String value() default "";
    
    ///
    /// English translation for the config value.
    /// By default, the config key will be used with {@link SLStringUtils#toTitleCase(String)}
    ///
    /// @return English translation for the config value.
    ///
    String translation() default "";
    
    ///
    /// Comment for the config value. Can be multiline.
    ///
    /// @return Comment for the config value.
    ///
    String comment() default "";
}
