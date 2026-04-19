package dev.satherov.sathlib.client.lang;

import dev.satherov.sathlib.network.chat.SLComponent;

///
/// Marks a class as being able to be displayed as a component
///
public interface SLDisplayable {
    
    ///
    /// Converts the given object into a translatable component.
    ///
    /// The implementing class should check the validity of the
    /// given arguments and comment which types are valid for it
    ///
    /// @param args implementation-defined formatting or lookup arguments
    ///
    /// @return Translatable component
    ///
    SLComponent display(Object... args);
}
