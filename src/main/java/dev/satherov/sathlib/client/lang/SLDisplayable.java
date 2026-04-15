package dev.satherov.sathlib.client.lang;

import net.minecraft.network.chat.MutableComponent;

public interface SLDisplayable {
    
    ///
    /// Converts the given object into a translatable component
    ///
    /// @return Translatable component
    ///
    MutableComponent display(Object... args);
}
