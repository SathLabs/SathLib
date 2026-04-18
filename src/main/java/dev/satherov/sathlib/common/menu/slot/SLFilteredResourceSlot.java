package dev.satherov.sathlib.common.menu.slot;

import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

///
/// Handler-backed slot with a simple stack predicate.
///
/// Subclasses can either pass a predicate directly or override `mayPlace(...)`
/// again for more elaborate rules.
///
/// - keep common slot filtering logic reusable
/// - compose item-acceptance rules without screen involvement
///
public class SLFilteredResourceSlot extends SLResourceSlot {
    
    private final Predicate<ItemStack> stackFilter;
    
    ///
    /// Creates a filtered handler-backed slot.
    ///
    /// @param handler      backing resource handler
    /// @param slotModifier direct slot mutation hook for the handler
    /// @param slotIndex    backing slot index
    /// @param stackFilter  predicate deciding whether a stack may be placed
    ///
    public SLFilteredResourceSlot(
            ResourceHandler<ItemResource> handler,
            IndexModifier<ItemResource> slotModifier,
            int slotIndex,
            Predicate<ItemStack> stackFilter
    ) {
        super(handler, slotModifier, slotIndex);
        this.stackFilter = Objects.requireNonNull(stackFilter);
    }
    
    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.stackFilter.test(stack) && super.mayPlace(stack);
    }
}
