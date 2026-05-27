package dev.satherov.sathlib.common.menu.logic;

import lombok.Getter;

import java.util.Objects;

///
/// Container node that assigns one slot key to its descendant slots.
///
/// Groups let menus describe sections such as machine input, player inventory,
/// or hotbar once and then reuse that key in quick-move logic and client slot
/// layout.
///
/// - tag descendant slots with one logical meaning
/// - keep menu trees readable
/// - bridge common-side logic to client-side slot positioning
///
public final class SLMenuGroupNode extends SLMenuContainerNode {
    
    private final @Getter SLSlotKey slotKey;
    
    ///
    /// Creates a group for the given slot key.
    ///
    /// @param slotKey slot key assigned to descendant slots
    ///
    public SLMenuGroupNode(SLSlotKey slotKey) {
        this.slotKey = Objects.requireNonNull(slotKey);
    }
}
