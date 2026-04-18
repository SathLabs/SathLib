package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.world.item.ItemStack;

///
/// Slot contract for custom display behavior.
///
/// Implementations take a logical slot stack and produce a client-neutral
/// rendering snapshot. That lets slot classes decide how their contents should
/// appear without requiring every screen to special-case slot subclasses.
///
/// - move slot display behavior into slot classes
/// - keep screen rendering generic
/// - preserve common/client separation
///
public interface SLDisplaySlot {
    
    ///
    /// Creates rendering data for the passed logical stack state.
    ///
    /// @param stack logical stack state to render
    ///
    /// @return rendering snapshot for the slot
    ///
    SLSlotRenderData createRenderData(ItemStack stack);
}
