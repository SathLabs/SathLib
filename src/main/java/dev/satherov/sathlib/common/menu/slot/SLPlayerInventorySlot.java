package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.world.entity.player.Inventory;

///
/// Standard player inventory slot with SathLib visuals.
///
/// This represents the main inventory rows, not the hotbar.
///
/// - keep player-side slot classes explicit
/// - give the menu screen a stable visual identity per slot type
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
    
    @Override
    protected SLSlotVisuals getSlotVisuals() {
        return SLSlotVisuals.PLAYER;
    }
}
