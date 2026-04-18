package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

///
/// Base SathLib slot for vanilla container-backed inventories.
///
/// The constructor still satisfies the vanilla base class, but real placement
/// stays entirely on the client side. Subclasses usually override
/// `mayPlace(...)` and the display hooks below.
///
/// - provide a reusable common slot base with custom display support
/// - keep real slot placement out of the menu layer
/// - support player inventory, hotbar, and container-backed custom slots
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
    
    @Override
    public SLSlotRenderData createRenderData(ItemStack stack) {
        ItemStack displayStack = this.getDisplayStack(stack);
        return new SLSlotRenderData(
                displayStack,
                this.getTooltipStack(stack, displayStack),
                this.getItemCountText(stack, displayStack),
                this.isFakeStack(stack, displayStack),
                this.getEmptyIcon(stack, displayStack),
                this.getSlotVisuals()
        );
    }
    
    ///
    /// Returns the stack that should be rendered for the passed logical state.
    ///
    /// @param stack logical stack state
    ///
    /// @return rendered stack
    ///
    protected ItemStack getDisplayStack(ItemStack stack) {
        return stack;
    }
    
    ///
    /// Returns the tooltip stack for the passed logical and display state.
    ///
    /// @param logicalStack logical slot stack
    /// @param displayStack rendered stack
    ///
    /// @return tooltip stack, or {@code null} for no tooltip
    ///
    protected @Nullable ItemStack getTooltipStack(ItemStack logicalStack, ItemStack displayStack) {
        if (!logicalStack.isEmpty()) return logicalStack;
        return displayStack.isEmpty() ? null : displayStack;
    }
    
    ///
    /// Returns an optional item count label override.
    ///
    /// @param logicalStack logical slot stack
    /// @param displayStack rendered stack
    ///
    /// @return count label override, or {@code null}
    ///
    protected @Nullable String getItemCountText(ItemStack logicalStack, ItemStack displayStack) {
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
    protected boolean isFakeStack(ItemStack logicalStack, ItemStack displayStack) {
        return this.isFake();
    }
    
    ///
    /// Returns the empty icon that should be drawn when no stack is rendered.
    ///
    /// @param logicalStack logical slot stack
    /// @param displayStack rendered stack
    ///
    /// @return empty icon, or {@code null}
    ///
    protected @Nullable Identifier getEmptyIcon(ItemStack logicalStack, ItemStack displayStack) {
        return displayStack.isEmpty() ? this.getNoItemIcon() : null;
    }
    
    ///
    /// Returns the frame visuals for this slot.
    ///
    /// @return slot frame visuals
    ///
    protected SLSlotVisuals getSlotVisuals() {
        return SLSlotVisuals.DEFAULT;
    }
}
