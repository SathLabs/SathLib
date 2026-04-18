package dev.satherov.sathlib.common.menu.sync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// Marks a menu field for automatic client/server synchronization.
///
/// SathLib scans annotated fields on a menu instance and binds the matching
/// `DataSlot` objects automatically. That means menu code can expose normal
/// typed fields and getters without managing raw sync indices.
///
/// Supported field types are:
///
/// - `boolean`
/// - `byte`
/// - `short`
/// - `int`
/// - `long`
/// - `float`
/// - `double`
/// - enum types
///
/// Annotated fields must not be `static` or `final`.
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SyncedField { }
