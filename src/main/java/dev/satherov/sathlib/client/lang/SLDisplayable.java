package dev.satherov.sathlib.client.lang;

import dev.satherov.sathlib.network.chat.SLComponent;

public interface SLDisplayable {
    
    ///
    /// Converts the given object into a translatable component
    ///
    /// @return Translatable component
    ///
    SLComponent display(Object... args);
}
