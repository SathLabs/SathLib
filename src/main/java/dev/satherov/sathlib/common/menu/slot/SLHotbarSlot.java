package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.world.entity.player.Inventory;

///
/// Standard hotbar slot marker for SathLib menus.
///
/// The client can use the runtime type to choose a matching slot chrome, but
/// the slot itself does not force any rendering style.
///
public class SLHotbarSlot extends SLPlayerInventorySlot {
    
    ///
    /// Creates a hotbar slot for one inventory index.
    ///
    /// @param inventory player inventory
    /// @param slotIndex hotbar slot index
    ///
    public SLHotbarSlot(Inventory inventory, int slotIndex) {
        super(inventory, slotIndex);
    }
}
