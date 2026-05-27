package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

///
/// Slot contract for custom display behavior.
///
/// Implementations transform a logical slot stack into a client-neutral render
/// snapshot. The slot decides what item appears inside the bounds and what
/// tooltip item should be exposed, while the client-side slot component decides
/// where the slot renders and what frame chrome it uses.
///
public interface SLDisplaySlot {
    
    ///
    /// Creates render data for the passed logical slot state.
    ///
    /// @param stack logical stack state to render
    ///
    /// @return immutable rendering snapshot
    ///
    default SLSlotRenderData createRenderData(ItemStack stack) {
        ItemStack displayStack = this.getDisplayStack(stack);
        return SLSlotRenderData.builder(displayStack)
                .tooltipStack(this.getTooltipStack(stack, displayStack))
                .itemCountText(this.getItemCountText(stack, displayStack))
                .fakeStack(this.isFakeStack(stack, displayStack))
                .emptyIcon(this.getEmptyIcon(stack, displayStack))
                .build();
    }
    
    ///
    /// Returns the stack that should be rendered for the passed logical state.
    ///
    /// @param stack logical slot stack
    ///
    /// @return rendered stack
    ///
    default ItemStack getDisplayStack(ItemStack stack) {
        return stack;
    }
    
    ///
    /// Returns the tooltip stack for the passed logical and display state.
    ///
    /// @param logicalStack logical slot stack
    /// @param displayStack rendered stack
    ///
    /// @return tooltip stack, or {@code null}
    ///
    default @Nullable ItemStack getTooltipStack(ItemStack logicalStack, ItemStack displayStack) {
        if (!logicalStack.isEmpty()) {
            return logicalStack;
        }
        return displayStack.isEmpty() ? null : displayStack;
    }
    
    ///
    /// Returns an optional item-count label override.
    ///
    /// @param logicalStack logical slot stack
    /// @param displayStack rendered stack
    ///
    /// @return count label override, or {@code null}
    ///
    default @Nullable String getItemCountText(ItemStack logicalStack, ItemStack displayStack) {
        return null;
    }
    
    ///
    /// Returns whether the rendered stack should use the fake-item render path.
    ///
    /// @param logicalStack logical slot stack
    /// @param displayStack rendered stack
    ///
    /// @return {@code true} when the fake-item path should be used
    ///
    default boolean isFakeStack(ItemStack logicalStack, ItemStack displayStack) {
        return this.slotView().isFake();
    }
    
    ///
    /// Returns the empty icon that should be drawn when no stack is rendered.
    ///
    /// @param logicalStack logical slot stack
    /// @param displayStack rendered stack
    ///
    /// @return empty icon, or {@code null}
    ///
    default @Nullable Identifier getEmptyIcon(ItemStack logicalStack, ItemStack displayStack) {
        return displayStack.isEmpty() ? this.slotView().getNoItemIcon() : null;
    }
    
    ///
    /// Returns this display slot as a vanilla slot instance.
    ///
    /// @return vanilla slot view
    ///
    private Slot slotView() {
        if (this instanceof Slot slot) {
            return slot;
        }
        throw new IllegalStateException("SLDisplaySlot implementations must also extend Slot.");
    }
}
