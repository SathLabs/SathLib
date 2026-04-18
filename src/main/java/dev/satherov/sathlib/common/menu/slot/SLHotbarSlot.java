package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.world.entity.player.Inventory;

///
/// Standard hotbar slot with SathLib visuals.
///
/// - keep hotbar slots distinct from the main inventory rows
/// - allow hotbar visuals to diverge without screen-side branching
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
    
    @Override
    protected SLSlotVisuals getSlotVisuals() {
        return SLSlotVisuals.HOTBAR;
    }
}
