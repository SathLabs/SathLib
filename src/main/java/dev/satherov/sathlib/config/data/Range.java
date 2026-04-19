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
    
    double min() default Double.NEGATIVE_INFINITY;
    
    double max() default Double.POSITIVE_INFINITY;
}
