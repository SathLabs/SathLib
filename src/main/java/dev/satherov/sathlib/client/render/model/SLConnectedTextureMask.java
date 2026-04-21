package dev.satherov.sathlib.client.render.model;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks an integer as a connected-texture bit mask composed from
/// {@link SLConnectedTextureMasks} flags.
///
/// This is intentionally a source-retained annotation. It exists to give the
/// IDE enough information to treat raw {@code int} values as a constrained mask
/// domain without affecting runtime behavior or serialized data.
///
/// IntelliJ understands this annotation through {@link MagicConstant} and
/// will flag invalid literals or incompatible bit combinations at call sites.
/// That makes APIs such as {@code regionForMask(...)} and
/// {@code fallbackMask(...)} much harder to misuse accidentally.
@Documented
@MagicConstant(flagsFromClass = SLConnectedTextureMasks.class)
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE_USE,
        ElementType.PARAMETER,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE
})
public @interface SLConnectedTextureMask { }
