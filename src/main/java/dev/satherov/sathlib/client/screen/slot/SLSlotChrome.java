package dev.satherov.sathlib.client.screen.slot;

///
/// Client-side frame chrome for rendered menu slots.
///
/// Slot chrome is a purely client concern. It defines how the shared theme
/// should draw one slot frame once a screen component has decided where that
/// slot lives.
///
/// @param drawFrame   whether a frame should be drawn at all
/// @param fillColor   frame body color
/// @param borderColor frame outline color
///
public record SLSlotChrome(boolean drawFrame, int fillColor, int borderColor) { }
