package dev.satherov.sathlib.common.menu.logic;

import java.util.Objects;

///
/// Describes the logical meaning of one or more menu slots.
///
/// Menus use semantics to classify slots for quick-move logic and for client
/// screens that want to position groups of slots without caring about their raw
/// indices.
///
/// - identify a slot group's role
/// - describe whether the group belongs to the player side
/// - act as the common key shared by menu logic and screen layout
///
/// Create mod-specific semantics for custom machine slots when the built-in
/// ones are not expressive enough.
///
/// @param id         stable semantic identifier
/// @param playerSide whether the semantic belongs to the player inventory side
///
public record SLSlotSemantic(String id, boolean playerSide) {
    
    ///
    /// Validates the semantic identifier.
    ///
    public SLSlotSemantic {
        Objects.requireNonNull(id);
    }
}
