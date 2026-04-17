package dev.satherov.sathlib.common.properties;

import dev.satherov.sathlib.client.lang.SLDisplayable;

import net.minecraft.network.chat.MutableComponent;

public interface PropertyEnum extends SLDisplayable {
    
    ///
    /// The tooltip for the current enum entry
    ///
    /// @return Tooltip for the current enum entry
    ///
    MutableComponent tooltip();
}
