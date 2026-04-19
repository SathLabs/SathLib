package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLScalar;

import net.minecraft.client.gui.Font;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Objects;

///
/// Layout-only node that positions one semantic group of menu slots.
///
/// The node participates in the retained-mode layout pass but does not draw any
/// visuals itself. Instead, it resolves concrete bounds for each slot so the
/// screen can render and hit-test them without mutating vanilla slot state.
///
/// - measure slot groups as semantic content
/// - assign per-slot bounds during layout
/// - keep slot positioning fully on the client side
///
public class SLMenuSlotGridNode extends UILeafNode<SLMenuSlotGridNode> {
    
    /// Outer frame size for one slot in pixels.
    public static final int SLOT_FRAME_SIZE = 18;
    /// Offset from the frame edge to the item content area in pixels.
    public static final int SLOT_CONTENT_OFFSET = 1;
    /// Content area size inside one slot frame in pixels.
    public static final int SLOT_CONTENT_SIZE = 16;
    
    private final List<Slot> slots;
    private List<SLBounds> resolvedSlotBounds = List.of();
    
    private int columns;
    private SLScalar gap = SLScalar.zero();
    
    ///
    /// Creates a slot grid node for one ordered slot list.
    ///
    /// @param slots   slot list to position
    /// @param columns amount of columns in the grid
    ///
    public SLMenuSlotGridNode(List<Slot> slots, int columns) {
        this.slots = List.copyOf(Objects.requireNonNull(slots));
        this.columns = Math.max(1, columns);
    }
    
    ///
    /// Returns the ordered slot list owned by this layout node.
    ///
    /// @return immutable slot list
    ///
    public List<Slot> getSlots() {
        return this.slots;
    }
    
    ///
    /// Returns the resolved frame bounds for each slot.
    ///
    /// The bounds list matches the order of {@link #getSlots()} and is empty
    /// until the node has been laid out.
    ///
    /// @return immutable per-slot frame bounds
    ///
    public List<SLBounds> getResolvedSlotBounds() {
        return this.resolvedSlotBounds;
    }
    
    ///
    /// Sets the amount of columns used by the grid.
    ///
    /// @param columns new grid column count
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode columns(int columns) {
        this.columns = Math.max(1, columns);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets extra spacing between slot frames.
    ///
    /// @param gap semantic gap value
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode gap(SLScalar gap) {
        this.gap = Objects.requireNonNull(gap);
        this.invalidateLayout();
        return this.self();
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        this.clearResolvedSlotBounds();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            this.clearResolvedSlotBounds();
        }
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        if (this.slots.isEmpty()) {
            return SLMeasuredSize.ZERO;
        }
        
        int columnCount = Math.min(this.columns, this.slots.size());
        int rowCount = (this.slots.size() + this.columns - 1) / this.columns;
        int gapPixels = this.gap.resolve(Math.max(availableWidth, availableHeight));
        
        int width = (columnCount * SLMenuSlotGridNode.SLOT_FRAME_SIZE) + (Math.max(0, columnCount - 1) * gapPixels);
        int height = (rowCount * SLMenuSlotGridNode.SLOT_FRAME_SIZE) + (Math.max(0, rowCount - 1) * gapPixels);
        return new SLMeasuredSize(width, height);
    }
    
    @Override
    protected void onLayout(Font font, SLBounds contentBounds) {
        if (!this.isVisible() || this.slots.isEmpty()) {
            this.clearResolvedSlotBounds();
            return;
        }
        
        int gapPixels = this.gap.resolve(Math.max(contentBounds.width(), contentBounds.height()));
        SLBounds[] resolvedBounds = new SLBounds[this.slots.size()];
        
        for (int slotIndex = 0; slotIndex < this.slots.size(); slotIndex++) {
            int column = slotIndex % this.columns;
            int row = slotIndex / this.columns;
            int cellX = contentBounds.x() + (column * (SLMenuSlotGridNode.SLOT_FRAME_SIZE + gapPixels));
            int cellY = contentBounds.y() + (row * (SLMenuSlotGridNode.SLOT_FRAME_SIZE + gapPixels));
            resolvedBounds[slotIndex] = new SLBounds(
                    cellX,
                    cellY,
                    SLMenuSlotGridNode.SLOT_FRAME_SIZE,
                    SLMenuSlotGridNode.SLOT_FRAME_SIZE
            );
        }
        
        this.resolvedSlotBounds = List.of(resolvedBounds);
    }
    
    @Override
    protected void onDetached() {
        this.clearResolvedSlotBounds();
    }
    
    private void clearResolvedSlotBounds() {
        this.resolvedSlotBounds = List.of();
    }
}
