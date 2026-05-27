package dev.satherov.sathlib.common.menu.logic;

import net.minecraft.world.inventory.Slot;

import java.util.Objects;

///
/// Leaf node that contributes one runtime slot to a menu logic tree.
///
/// Slot nodes define what slot instance should exist, while an enclosing
/// {@link SLMenuGroupNode} defines what that slot means logically.
///
/// - hold one slot factory
/// - keep slot creation free of screen coordinates
///
public final class SLMenuSlotNode extends SLMenuNode {
    
    private final SLMenuSlotFactory slotFactory;
    
    ///
    /// Creates a slot node backed by the given factory.
    ///
    /// @param slotFactory runtime slot factory
    ///
    public SLMenuSlotNode(SLMenuSlotFactory slotFactory) {
        this.slotFactory = Objects.requireNonNull(slotFactory);
    }
    
    ///
    /// Creates the runtime slot for this node.
    ///
    /// @return new slot instance
    ///
    public Slot createSlot() {
        return this.slotFactory.createSlot();
    }
}
