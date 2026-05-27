package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

///
/// Base SathLib slot for vanilla container-backed inventories.
///
/// The constructor still satisfies the vanilla base class, but real placement
/// stays entirely on the client side. Subclasses typically override the
/// display hooks declared by {@link SLDisplaySlot} or `mayPlace(...)`.
///
public class SLContainerSlot extends Slot implements SLDisplaySlot {
    
    private static final int PLACEHOLDER_POSITION = 0;
    
    ///
    /// Creates a container-backed slot.
    ///
    /// @param container backing container
    /// @param slotIndex backing slot index
    ///
    public SLContainerSlot(Container container, int slotIndex) {
        super(container, slotIndex, SLContainerSlot.PLACEHOLDER_POSITION, SLContainerSlot.PLACEHOLDER_POSITION);
    }
}
