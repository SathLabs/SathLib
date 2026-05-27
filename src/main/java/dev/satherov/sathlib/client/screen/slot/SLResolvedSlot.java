package dev.satherov.sathlib.client.screen.slot;

import dev.satherov.sathlib.client.screen.layout.SLBounds;

///
/// Resolved client-side bounds and chrome for one menu slot.
///
/// Slot components produce these during layout so the shared menu screen can
/// render, hit-test, and animate slots without mutating the vanilla slot
/// coordinates.
///
/// @param frameBounds   outer slot-frame bounds
/// @param contentBounds inner item-render bounds
/// @param chrome        client-side frame chrome
///
public record SLResolvedSlot(SLBounds frameBounds, SLBounds contentBounds, SLSlotChrome chrome) { }
