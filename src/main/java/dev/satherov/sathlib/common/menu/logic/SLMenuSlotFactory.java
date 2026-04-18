package dev.satherov.sathlib.common.menu.logic;

import net.minecraft.world.inventory.Slot;

///
/// Creates one runtime slot for a menu logic node.
///
/// Implementations should return a fresh slot instance every time the factory
/// is invoked.
///
/// - isolate slot construction from screen layout
/// - allow menu logic trees to stay coordinate-free
///
@FunctionalInterface
public interface SLMenuSlotFactory {
    
    ///
    /// Creates a runtime slot instance.
    ///
    /// @return new slot instance
    ///
    Slot createSlot();
}
