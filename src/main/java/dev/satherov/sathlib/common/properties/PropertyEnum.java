package dev.satherov.sathlib.common.properties;

import dev.satherov.sathlib.client.lang.SLDisplayable;
import dev.satherov.sathlib.network.chat.SLComponent;

///
/// Marker for enum-backed property values that expose tooltip text.
///
public interface PropertyEnum extends SLDisplayable {
    
    ///
    /// The tooltip for the current enum entry
    ///
    /// @return Tooltip for the current enum entry
    ///
    SLComponent tooltip();
}
