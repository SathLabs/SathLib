package dev.satherov.sathlib.common.menu.logic;

import dev.satherov.sathlib.SathLib;

import net.minecraft.resources.Identifier;

///
/// Built-in slot keys shared by SathLib menus.
///
/// Mods can define their own keys through {@link #create(Identifier, boolean)}
/// whenever a menu needs a more specific slot grouping.
///
public final class SLSlotKeys {
    
    /// Slot key for the player's main inventory rows.
    public static final SLSlotKey PLAYER_INVENTORY = SLSlotKeys.create("player_inventory", true);
    /// Slot key for the player's hotbar.
    public static final SLSlotKey PLAYER_HOTBAR = SLSlotKeys.create("player_hotbar", true);
    /// Slot key for machine input slots.
    public static final SLSlotKey MACHINE_INPUT = SLSlotKeys.create("machine_input", false);
    /// Slot key for machine output slots.
    public static final SLSlotKey MACHINE_OUTPUT = SLSlotKeys.create("machine_output", false);
    /// Slot key for generic machine storage.
    public static final SLSlotKey MACHINE_STORAGE = SLSlotKeys.create("machine_storage", false);
    /// Slot key for configuration slots.
    public static final SLSlotKey CONFIG = SLSlotKeys.create("config", false);
    /// Slot key for upgrade slots.
    public static final SLSlotKey UPGRADE = SLSlotKeys.create("upgrade", false);
    
    ///
    /// Creates a custom key under the `sathlib` namespace.
    ///
    /// @param identifier stable slot-group identifier
    /// @param playerSide whether the slot group belongs to the player side
    ///
    private static SLSlotKey create(String identifier, boolean playerSide) {
        return new SLSlotKey(SathLib.id(identifier), playerSide);
    }
    
    ///
    /// Creates a custom slot key.
    ///
    /// @param identifier stable slot-group identifier
    /// @param playerSide whether the slot group belongs to the player side
    ///
    /// @return new slot key
    ///
    public static SLSlotKey create(Identifier identifier, boolean playerSide) {
        return new SLSlotKey(identifier, playerSide);
    }
}
