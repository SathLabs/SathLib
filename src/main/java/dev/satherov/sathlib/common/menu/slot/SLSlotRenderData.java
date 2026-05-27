package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

///
/// Immutable client-neutral rendering snapshot for a menu slot.
///
/// The slot class creates one of these from its logical contents and the shared
/// menu screen consumes it generically. The record only describes item content
/// and tooltip behavior; frame chrome now belongs entirely to client-side slot
/// components.
///
/// @param displayStack  stack that should be rendered in the slot
/// @param tooltipStack  stack that should be used for tooltips, or {@code null}
/// @param itemCountText optional item-count text override
/// @param fakeStack     whether the stack should use the fake-item render path
/// @param emptyIcon     optional empty-slot icon
///
public record SLSlotRenderData(
        ItemStack displayStack,
        @Nullable ItemStack tooltipStack,
        @Nullable String itemCountText,
        boolean fakeStack,
        @Nullable Identifier emptyIcon
) {
    
    ///
    /// Creates a new builder seeded with the passed display stack.
    ///
    /// @param displayStack initial display stack
    ///
    /// @return new builder
    ///
    public static Builder builder(ItemStack displayStack) {
        return new Builder(displayStack);
    }
    
    ///
    /// Fluent builder for slot render data.
    ///
    public static final class Builder {
        
        private final ItemStack displayStack;
        private @Nullable ItemStack tooltipStack;
        private @Nullable String itemCountText;
        private boolean fakeStack;
        private @Nullable Identifier emptyIcon;
        
        ///
        /// Creates a new builder seeded with one display stack.
        ///
        /// @param displayStack initial display stack
        ///
        private Builder(ItemStack displayStack) {
            this.displayStack = Objects.requireNonNull(displayStack);
        }
        
        ///
        /// Overrides the tooltip stack.
        ///
        /// @param tooltipStack tooltip stack, or {@code null}
        ///
        /// @return this builder
        ///
        public Builder tooltipStack(@Nullable ItemStack tooltipStack) {
            this.tooltipStack = tooltipStack;
            return this;
        }
        
        ///
        /// Overrides the item-count text.
        ///
        /// @param itemCountText item-count text, or {@code null}
        ///
        /// @return this builder
        ///
        public Builder itemCountText(@Nullable String itemCountText) {
            this.itemCountText = itemCountText;
            return this;
        }
        
        ///
        /// Sets whether the fake-item render path should be used.
        ///
        /// @param fakeStack fake-item flag
        ///
        /// @return this builder
        ///
        public Builder fakeStack(boolean fakeStack) {
            this.fakeStack = fakeStack;
            return this;
        }
        
        ///
        /// Sets the empty-slot icon.
        ///
        /// @param emptyIcon empty-slot icon, or {@code null}
        ///
        /// @return this builder
        ///
        public Builder emptyIcon(@Nullable Identifier emptyIcon) {
            this.emptyIcon = emptyIcon;
            return this;
        }
        
        ///
        /// Builds the immutable render snapshot.
        ///
        /// @return built slot render data
        ///
        public SLSlotRenderData build() {
            return new SLSlotRenderData(this.displayStack, this.tooltipStack, this.itemCountText, this.fakeStack, this.emptyIcon);
        }
    }
}
