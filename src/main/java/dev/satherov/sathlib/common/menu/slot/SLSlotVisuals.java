package dev.satherov.sathlib.common.menu.slot;

///
/// Client-neutral visual hints for a menu slot.
///
/// Slot classes use this value object to describe their frame treatment without
/// depending on any client-only rendering types. The actual drawing still
/// happens on the client through the shared menu screen and theme.
///
/// - keep slot display customization out of screen switch statements
/// - let slot classes describe their own frame treatment
/// - keep common and client code separated
///
/// @param drawFrame   whether the menu screen should draw the slot frame
/// @param fillColor   fill color used for the frame body
/// @param borderColor border color used for the frame outline
///
public record SLSlotVisuals(boolean drawFrame, int fillColor, int borderColor) {
    
    public static final SLSlotVisuals DEFAULT = new SLSlotVisuals(true, 0xFF10151C, 0xFF48556B);
    public static final SLSlotVisuals PLAYER = new SLSlotVisuals(true, 0xFF0E131A, 0xFF445066);
    public static final SLSlotVisuals HOTBAR = new SLSlotVisuals(true, 0xFF0E131A, 0xFF5A6984);
    public static final SLSlotVisuals MACHINE = new SLSlotVisuals(true, 0xFF111924, 0xFF6D8BB0);
    public static final SLSlotVisuals FRAMELESS = new SLSlotVisuals(false, 0, 0);
}
