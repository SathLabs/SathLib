package dev.satherov.sathlib.common.menu.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

///
/// Immutable client-neutral rendering snapshot for a menu slot.
///
/// The slot class creates one of these from its logical contents, and the menu
/// screen consumes it through the shared slot renderer. This keeps slot-driven
/// rendering polymorphic without pushing client-only types into common code.
///
/// - allow slots to alter how contents are shown
/// - expose optional tooltip and count-text overrides
/// - keep rendering decisions owned by slot classes
///
/// @param displayStack  stack that should be rendered in the slot
/// @param tooltipStack  stack that should be used for tooltips, or {@code null}
/// @param itemCountText optional item count text override
/// @param fakeStack     whether the stack should render through the fake-item path
/// @param emptyIcon     optional empty-slot icon
/// @param visuals       visual hints for the slot frame
///
public record SLSlotRenderData(
        ItemStack displayStack,
        @Nullable ItemStack tooltipStack,
        @Nullable String itemCountText,
        boolean fakeStack,
        @Nullable Identifier emptyIcon,
        SLSlotVisuals visuals
) {
}
