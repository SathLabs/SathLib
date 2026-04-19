package dev.satherov.sathlib.common.properties;

import dev.satherov.sathlib.client.lang.SLDisplayable;

import net.minecraft.network.chat.MutableComponent;

///
/// Marker for enum-backed property values that expose tooltip text.
///
public interface PropertyEnum extends SLDisplayable {
    
    ///
    /// The tooltip for the current enum entry
    ///
    /// @return Tooltip for the current enum entry
    ///
    MutableComponent tooltip();
}
