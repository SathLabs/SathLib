package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;
import lombok.Getter;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.slot.SLResolvedSlot;
import dev.satherov.sathlib.client.screen.slot.SLSlotChrome;
import dev.satherov.sathlib.client.screen.slot.SLSlotChromes;
import dev.satherov.sathlib.client.screen.slot.SLSlotLayoutNode;

import net.minecraft.client.gui.Font;
import net.minecraft.world.inventory.Slot;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

///
/// Layout-only node that positions one ordered group of menu slots.
///
/// The node participates in the retained-mode layout pass but does not draw
/// any visuals itself. Instead, it resolves concrete slot rectangles so the
/// shared menu screen can render contents, draw slot chrome, and hit-test them
/// without mutating vanilla slot coordinates.
///
public class SLMenuSlotGridNode extends UILeafNode<SLMenuSlotGridNode> implements SLSlotLayoutNode {
    
    /// Default outer frame size for one slot in pixels.
    public static final int DEFAULT_SLOT_SIZE = 18;
    /// Default inset from the slot frame into the item content area.
    public static final int DEFAULT_CONTENT_INSET = 1;
    
    private final @Getter List<Slot> slots;
    private List<SLResolvedSlot> resolvedSlots = List.of();
    
    private int columns;
    private int slotSize;
    private int contentInset;
    private SLScalar gap;
    private SLSlotChrome chrome;
    private @Nullable Function<Slot, SLSlotChrome> chromeResolver;
    
    ///
    /// Creates an empty slot grid node.
    ///
    public SLMenuSlotGridNode() {
        this(SLModifier.none(), List.of(), 1, SLMenuSlotGridNode.DEFAULT_SLOT_SIZE, SLMenuSlotGridNode.DEFAULT_CONTENT_INSET, SLScalar.zero(), SLSlotChromes.DEFAULT, SLSlotChromes::defaultFor);
    }
    
    ///
    /// Creates a fully configured slot grid.
    ///
    /// @param modifier       node modifier
    /// @param slots          slot list to position
    /// @param columns        grid column count
    /// @param slotSize       slot-frame size in pixels
    /// @param contentInset   content inset in pixels
    /// @param gap            gap between slot frames
    /// @param chrome         fallback slot chrome
    /// @param chromeResolver optional per-slot chrome resolver
    ///
    protected SLMenuSlotGridNode(
            SLModifier modifier,
            List<Slot> slots,
            int columns,
            int slotSize,
            int contentInset,
            SLScalar gap,
            SLSlotChrome chrome,
            @Nullable Function<Slot, SLSlotChrome> chromeResolver
    ) {
        super(modifier);
        this.slots = List.copyOf(Objects.requireNonNull(slots));
        this.columns = Math.max(1, columns);
        this.slotSize = Math.max(1, slotSize);
        this.contentInset = Math.max(0, contentInset);
        this.gap = Objects.requireNonNull(gap);
        this.chrome = Objects.requireNonNull(chrome);
        this.chromeResolver = chromeResolver;
    }
    
    ///
    /// Creates a builder-backed slot grid while normalizing omitted values to
    /// framework defaults.
    ///
    /// @param modifier       node modifier
    /// @param slots          slot list to position
    /// @param columns        grid column count
    /// @param slotSize       slot-frame size in pixels
    /// @param contentInset   content inset in pixels
    /// @param gap            gap between slot frames
    /// @param chrome         fallback slot chrome
    /// @param chromeResolver optional per-slot chrome resolver
    ///
    /// @return configured slot grid node
    ///
    @Builder
    public static SLMenuSlotGridNode of(
            SLModifier modifier,
            List<Slot> slots,
            Integer columns,
            Integer slotSize,
            Integer contentInset,
            SLScalar gap,
            SLSlotChrome chrome,
            Function<Slot, SLSlotChrome> chromeResolver
    ) {
        return new SLMenuSlotGridNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(slots, List.of()),
                Objects.requireNonNullElse(columns, 1),
                Objects.requireNonNullElse(slotSize, SLMenuSlotGridNode.DEFAULT_SLOT_SIZE),
                Objects.requireNonNullElse(contentInset, SLMenuSlotGridNode.DEFAULT_CONTENT_INSET),
                Objects.requireNonNullElse(gap, SLScalar.zero()),
                Objects.requireNonNullElse(chrome, SLSlotChromes.DEFAULT),
                chromeResolver != null ? chromeResolver : SLSlotChromes::defaultFor
        );
    }
    
    
    ///
    /// Sets the number of grid columns.
    ///
    /// @param columns new column count
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode columns(int columns) {
        this.columns = Math.max(1, columns);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the rendered slot size in pixels.
    ///
    /// @param slotSize new slot-frame size
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode slotSize(int slotSize) {
        this.slotSize = Math.max(1, slotSize);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the inset from the frame into the content area.
    ///
    /// @param contentInset new content inset in pixels
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode contentInset(int contentInset) {
        this.contentInset = Math.max(0, contentInset);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the gap between slot frames.
    ///
    /// @param gap new semantic gap value
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode gap(SLScalar gap) {
        this.gap = Objects.requireNonNullElse(gap, SLScalar.zero());
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the fallback chrome used when no resolver is installed.
    ///
    /// @param chrome new fallback chrome
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode chrome(SLSlotChrome chrome) {
        this.chrome = Objects.requireNonNullElse(chrome, SLSlotChromes.DEFAULT);
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the per-slot chrome resolver.
    ///
    /// @param chromeResolver per-slot chrome resolver, or {@code null}
    ///
    /// @return this node
    ///
    public SLMenuSlotGridNode chromeResolver(@Nullable Function<Slot, SLSlotChrome> chromeResolver) {
        this.chromeResolver = chromeResolver;
        this.invalidateLayout();
        return this.self();
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        this.clearResolvedSlots();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            this.clearResolvedSlots();
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
        
        int width = (columnCount * this.slotSize) + (Math.max(0, columnCount - 1) * gapPixels);
        int height = (rowCount * this.slotSize) + (Math.max(0, rowCount - 1) * gapPixels);
        return new SLMeasuredSize(width, height);
    }
    
    @Override
    protected void onLayout(Font font, SLBounds contentBounds) {
        if (!this.isVisible() || this.slots.isEmpty()) {
            this.clearResolvedSlots();
            return;
        }
        
        int gapPixels = this.gap.resolve(Math.max(contentBounds.width(), contentBounds.height()));
        SLResolvedSlot[] resolvedSlots = new SLResolvedSlot[this.slots.size()];
        
        for (int slotIndex = 0; slotIndex < this.slots.size(); slotIndex++) {
            int column = slotIndex % this.columns;
            int row = slotIndex / this.columns;
            int cellX = contentBounds.x() + (column * (this.slotSize + gapPixels));
            int cellY = contentBounds.y() + (row * (this.slotSize + gapPixels));
            SLBounds frameBounds = new SLBounds(cellX, cellY, this.slotSize, this.slotSize);
            SLBounds itemBounds = frameBounds.inset(this.contentInset);
            resolvedSlots[slotIndex] = new SLResolvedSlot(frameBounds, itemBounds, this.resolveChrome(this.slots.get(slotIndex)));
        }
        
        this.resolvedSlots = List.of(resolvedSlots);
    }
    
    @Override
    public void collectResolvedSlots(Map<Slot, SLResolvedSlot> resolvedSlots) {
        int resolvedCount = Math.min(this.slots.size(), this.resolvedSlots.size());
        for (int slotIndex = 0; slotIndex < resolvedCount; slotIndex++) {
            resolvedSlots.put(this.slots.get(slotIndex), this.resolvedSlots.get(slotIndex));
        }
    }
    
    @Override
    protected void onDetached() {
        this.clearResolvedSlots();
    }
    
    ///
    /// Resolves the chrome used for one slot.
    ///
    /// @param slot runtime slot
    ///
    /// @return resolved client-side chrome
    ///
    private SLSlotChrome resolveChrome(Slot slot) {
        if (this.chromeResolver != null) return Objects.requireNonNullElse(this.chromeResolver.apply(slot), this.chrome);
        return this.chrome;
    }
    
    ///
    /// Clears the resolved client-only slot data.
    ///
    private void clearResolvedSlots() {
        this.resolvedSlots = List.of();
    }
}
