package dev.satherov.sathlib.config.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// Clamps a numeric config value to a specific range.
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {
    
    ///
    /// Lower inclusive bound for the annotated config value.
    ///
    /// @return minimum allowed value
    ///
    double min() default Double.NEGATIVE_INFINITY;
    
    ///
    /// Upper inclusive bound for the annotated config value.
    ///
    /// @return maximum allowed value
    ///
    double max() default Double.POSITIVE_INFINITY;
}
