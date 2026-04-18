package dev.satherov.sathlib.common.menu.logic;

///
/// Container node that assigns one semantic role to its descendant slots.
///
/// Groups let menus describe sections such as machine input, player inventory,
/// or hotbar once and then reuse that semantic in quick-move logic and client
/// slot layout.
///
/// - tag descendant slots with one semantic meaning
/// - keep menu trees readable
/// - bridge common-side logic to client-side slot positioning
///
public final class SLMenuGroupNode extends SLMenuContainerNode {
    
    private final SLSlotSemantic semantic;
    
    ///
    /// Creates a group for the given semantic role.
    ///
    /// @param semantic slot semantic assigned to descendant slots
    ///
    public SLMenuGroupNode(SLSlotSemantic semantic) {
        this.semantic = semantic;
    }
    
    ///
    /// Returns the semantic role of this group.
    ///
    /// @return group semantic
    ///
    public SLSlotSemantic getSemantic() {
        return this.semantic;
    }
}
