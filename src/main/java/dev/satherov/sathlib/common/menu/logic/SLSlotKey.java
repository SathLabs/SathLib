package dev.satherov.sathlib.common.menu.logic;

import net.minecraft.resources.Identifier;

import java.util.Objects;

///
/// Stable logical identifier for one slot group inside a menu.
///
/// Slot keys are the contract between the common-side menu definition and the
/// client-side screen layout. They describe what a slot collection represents,
/// but never where it renders.
///
/// @param identifier stable slot-group identifier
/// @param playerSide whether the slot group belongs to the player inventory
///                   side of the menu
///
public record SLSlotKey(Identifier identifier, boolean playerSide) {
    
    ///
    /// Validates the slot-key identifier.
    ///
    public SLSlotKey {
        Objects.requireNonNull(identifier, "Slot-key identifiers must not be blank.");
    }
}
