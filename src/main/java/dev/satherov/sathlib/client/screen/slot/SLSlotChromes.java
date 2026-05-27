package dev.satherov.sathlib.client.screen.slot;

import dev.satherov.sathlib.common.menu.slot.SLHotbarSlot;
import dev.satherov.sathlib.common.menu.slot.SLPlayerInventorySlot;
import dev.satherov.sathlib.common.menu.slot.SLResourceSlot;

import net.minecraft.world.inventory.Slot;

///
/// Built-in client-side slot chrome presets.
///
/// Screens can use these constants directly or rely on
/// {@link #defaultFor(Slot)} to map common SathLib slot subclasses to a
/// sensible default frame treatment.
///
public final class SLSlotChromes {
    
    /// Generic slot chrome.
    public static final SLSlotChrome DEFAULT = new SLSlotChrome(true, 0xFF10151C, 0xFF48556B);
    /// Player inventory chrome.
    public static final SLSlotChrome PLAYER = new SLSlotChrome(true, 0xFF0E131A, 0xFF445066);
    /// Hotbar chrome.
    public static final SLSlotChrome HOTBAR = new SLSlotChrome(true, 0xFF0E131A, 0xFF5A6984);
    /// Machine slot chrome.
    public static final SLSlotChrome MACHINE = new SLSlotChrome(true, 0xFF111924, 0xFF6D8BB0);
    /// Chrome that intentionally renders no frame.
    public static final SLSlotChrome FRAMELESS = new SLSlotChrome(false, 0, 0);
    
    private SLSlotChromes() { }
    
    ///
    /// Resolves the default chrome for a runtime slot type.
    ///
    /// @param slot runtime slot
    ///
    /// @return matching default chrome
    ///
    public static SLSlotChrome defaultFor(Slot slot) {
        return switch (slot) {
            case SLHotbarSlot _ -> SLSlotChromes.HOTBAR;
            case SLPlayerInventorySlot _ -> SLSlotChromes.PLAYER;
            case SLResourceSlot _ -> SLSlotChromes.MACHINE;
            default -> SLSlotChromes.DEFAULT;
        };
    }
}
