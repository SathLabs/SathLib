package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.world.entity.player.Inventory;

///
/// Standard player inventory slot marker for SathLib menus.
///
/// This represents the main inventory rows, not the hotbar. The client can use
/// the runtime type to choose a matching slot chrome, but the slot itself does
/// not force any rendering style.
///
public class SLPlayerInventorySlot extends SLContainerSlot {
    
    ///
    /// Creates a player inventory slot for one inventory index.
    ///
    /// @param inventory player inventory
    /// @param slotIndex inventory slot index
    ///
    public SLPlayerInventorySlot(Inventory inventory, int slotIndex) {
        super(inventory, slotIndex);
    }
}
