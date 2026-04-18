package dev.satherov.sathlib.common.menu.logic;

///
/// Built-in semantic slot keys for SathLib menus.
///
/// These cover the common roles shared by many menus. Mods can create
/// additional semantics through {@link #create(String, boolean)} when a menu
/// needs more specific meaning than the built-ins provide.
///
/// - define reusable common slot roles
/// - keep quick-move plans readable
/// - give screens stable keys for client-side slot placement
///
public final class SLSlotSemantics {
    
    public static final SLSlotSemantic PLAYER_INVENTORY = SLSlotSemantics.create("player_inventory", true);
    public static final SLSlotSemantic PLAYER_HOTBAR = SLSlotSemantics.create("player_hotbar", true);
    public static final SLSlotSemantic MACHINE_INPUT = SLSlotSemantics.create("machine_input", false);
    public static final SLSlotSemantic MACHINE_OUTPUT = SLSlotSemantics.create("machine_output", false);
    public static final SLSlotSemantic MACHINE_STORAGE = SLSlotSemantics.create("machine_storage", false);
    public static final SLSlotSemantic CONFIG = SLSlotSemantics.create("config", false);
    public static final SLSlotSemantic UPGRADE = SLSlotSemantics.create("upgrade", false);
    
    private SLSlotSemantics() {
    }
    
    ///
    /// Creates a semantic key for a custom slot role.
    ///
    /// Use a mod-specific id such as `examplemod:fuel_input` when exposing a
    /// semantic from library or addon code.
    ///
    /// @param id         stable semantic identifier
    /// @param playerSide whether the slots belong to the player side
    ///
    /// @return new semantic key
    ///
    public static SLSlotSemantic create(String id, boolean playerSide) {
        return new SLSlotSemantic(id, playerSide);
    }
}
