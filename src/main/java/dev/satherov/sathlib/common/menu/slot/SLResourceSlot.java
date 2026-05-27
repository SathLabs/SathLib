package dev.satherov.sathlib.common.menu.slot;

import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

///
/// Base SathLib slot for NeoForge handler-backed item storage.
///
/// This wraps NeoForge's {@link ResourceHandlerSlot} so subclasses can own item
/// validation and content display behavior while the client-side screen owns
/// all layout and frame rendering.
///
public class SLResourceSlot extends ResourceHandlerSlot implements SLDisplaySlot {
    
    private static final int PLACEHOLDER_POSITION = 0;
    
    ///
    /// Creates a handler-backed slot.
    ///
    /// @param handler      backing resource handler
    /// @param slotModifier direct slot mutation hook for the handler
    /// @param slotIndex    backing slot index
    ///
    public SLResourceSlot(
            ResourceHandler<ItemResource> handler,
            IndexModifier<ItemResource> slotModifier,
            int slotIndex
    ) {
        super(handler, slotModifier, slotIndex, SLResourceSlot.PLACEHOLDER_POSITION, SLResourceSlot.PLACEHOLDER_POSITION);
    }
}
