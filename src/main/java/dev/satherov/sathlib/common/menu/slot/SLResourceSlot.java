package dev.satherov.sathlib.common.menu.slot;

import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

///
/// Base SathLib slot for handler-backed item storage.
///
/// This wraps NeoForge's {@link ResourceHandlerSlot} so subclasses can own item
/// validation and display behavior while the screen owns layout.
///
/// - provide a reusable base for handler-backed machine slots
/// - expose slot-specific display hooks
/// - keep placement out of common code
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
        super(
                handler,
                slotModifier,
                slotIndex,
                SLResourceSlot.PLACEHOLDER_POSITION,
                SLResourceSlot.PLACEHOLDER_POSITION
        );
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
    /// @return tooltip stack, or {@code null}
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
        return SLSlotVisuals.MACHINE;
    }
}
