package dev.satherov.sathlib.client.screen.slot;

import net.minecraft.world.inventory.Slot;

import java.util.Map;

///
/// Client-side contract for UI nodes that resolve menu slot positions.
///
/// Implementations participate in retained layout as normal UI nodes and then
/// expose the resolved slot rectangles back to {@code SLMenuScreen}.
///
public interface SLSlotLayoutNode {
    
    ///
    /// Adds every resolved slot owned by this node to the passed map.
    ///
    /// @param resolvedSlots target map receiving resolved slot data
    ///
    void collectResolvedSlots(Map<Slot, SLResolvedSlot> resolvedSlots);
}
