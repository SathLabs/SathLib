package dev.satherov.sathlib.common.menu;

import dev.satherov.sathlib.common.menu.logic.SLMenuContainerNode;
import dev.satherov.sathlib.common.menu.logic.SLMenuGroupNode;
import dev.satherov.sathlib.common.menu.logic.SLMenuNode;
import dev.satherov.sathlib.common.menu.logic.SLMenuSlotNode;
import dev.satherov.sathlib.common.menu.logic.SLQuickMovePlan;
import dev.satherov.sathlib.common.menu.logic.SLSlotKey;
import dev.satherov.sathlib.common.menu.sync.SLMenuSync;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

///
/// Base class for logic-only SathLib menus.
///
/// A concrete menu constructs its storage references, calls this base
/// constructor, explicitly registers synced values through {@link #sync()}, and
/// finally defines a logical slot tree. The menu declares what exists and how
/// slots are grouped, but never where the client should render them.
///
@NothingNull
public abstract class SLMenu extends AbstractContainerMenu {
    
    ///
    /// Player inventory bound to this menu instance.
    ///
    protected final Inventory playerInventory;
    private final Map<SLSlotKey, List<Slot>> slotsByKey = new LinkedHashMap<>();
    private final Map<Slot, SLSlotKey> keyBySlot = new IdentityHashMap<>();
    private final SLMenuSync sync;
    
    private @Nullable SLMenuContainerNode root;
    private @Nullable SLQuickMovePlan quickMovePlan;
    
    ///
    /// Creates the menu base and prepares the explicit sync helper.
    ///
    /// @param type            registered menu type
    /// @param containerId     vanilla container id
    /// @param playerInventory opening player inventory
    ///
    protected SLMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        super(type, containerId);
        this.playerInventory = Objects.requireNonNull(playerInventory);
        this.sync = new SLMenuSync(this::addDataSlot);
    }
    
    ///
    /// Returns the explicit sync helper used to register `DataSlot`s.
    ///
    /// Concrete menus should call this during construction after the backing
    /// values have been created.
    ///
    /// @return explicit sync helper
    ///
    protected final SLMenuSync sync() {
        return this.sync;
    }
    
    ///
    /// Returns the menu logic root, if one was defined.
    ///
    /// @return logic root, or {@code null} before definition
    ///
    public final @Nullable SLMenuContainerNode getRoot() {
        return this.root;
    }
    
    ///
    /// Returns all slots registered for one logical slot key.
    ///
    /// The returned list is immutable and preserves the registration order from
    /// the logical menu tree.
    ///
    /// @param slotKey key to query
    ///
    /// @return immutable slot list
    ///
    public final List<Slot> getSlots(SLSlotKey slotKey) {
        List<Slot> slots = this.slotsByKey.get(slotKey);
        if (slots == null) {
            return List.of();
        }
        return Collections.unmodifiableList(slots);
    }
    
    ///
    /// Returns the logical slot key for a runtime slot.
    ///
    /// @param slot runtime slot
    ///
    /// @return slot key, or {@code null} when unknown
    ///
    public final @Nullable SLSlotKey getSlotKey(Slot slot) {
        return this.keyBySlot.get(slot);
    }
    
    ///
    /// Returns the logical slot key for a slot index.
    ///
    /// @param slotIndex menu slot index
    ///
    /// @return slot key, or {@code null} when the index is invalid or unmapped
    ///
    public final @Nullable SLSlotKey getSlotKey(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return null;
        }
        return this.getSlotKey(this.slots.get(slotIndex));
    }
    
    ///
    /// Returns whether the given key belongs to the player side.
    ///
    /// @param slotKey key to test
    ///
    /// @return {@code true} when the key represents player inventory space
    ///
    public final boolean isPlayerSideKey(SLSlotKey slotKey) {
        return slotKey.playerSide();
    }
    
    ///
    /// Declares the retained logical slot tree for this menu.
    ///
    /// This method should be called exactly once from the concrete menu
    /// constructor after all backing storage references have been assigned.
    ///
    /// @param root logical root node
    ///
    protected final void defineMenu(SLMenuContainerNode root) {
        if (this.root != null) {
            throw new IllegalStateException("Menu logic was already defined for " + this.getClass().getName());
        }
        
        this.root = Objects.requireNonNull(root);
        this.root.attach(null);
        this.registerLogicNode(this.root, null);
    }
    
    ///
    /// Installs the shared quick-move plan for this menu.
    ///
    /// Menus with special transfer rules can still override
    /// {@link #quickMoveStack(Player, int)} directly.
    ///
    /// @param quickMovePlan logical quick-move route table
    ///
    protected final void setQuickMovePlan(SLQuickMovePlan quickMovePlan) {
        this.quickMovePlan = Objects.requireNonNull(quickMovePlan);
    }
    
    ///
    /// Adds one keyed slot directly.
    ///
    /// This is the low-level escape hatch behind the logical menu tree. Most
    /// menus should prefer {@link #defineMenu(SLMenuContainerNode)} with
    /// `SLMenus`.
    ///
    /// @param slot    runtime slot instance
    /// @param slotKey logical slot key
    ///
    /// @return the added slot
    ///
    protected final Slot addLogicalSlot(Slot slot, SLSlotKey slotKey) {
        Slot addedSlot = super.addSlot(slot);
        this.keyBySlot.put(addedSlot, slotKey);
        this.slotsByKey.computeIfAbsent(slotKey, ignored -> new ArrayList<>()).add(addedSlot);
        return addedSlot;
    }
    
    ///
    /// Attempts to move a stack into all slots that share one slot key.
    ///
    /// @param stack   mutable stack being moved
    /// @param slotKey destination slot key
    /// @param reverse whether the destination order should be reversed
    ///
    /// @return {@code true} when at least part of the stack moved
    ///
    protected final boolean moveToKey(ItemStack stack, SLSlotKey slotKey, boolean reverse) {
        return this.moveToSlots(stack, this.getSlots(slotKey), reverse);
    }
    
    ///
    /// Attempts to move a stack into an arbitrary slot list.
    ///
    /// The algorithm first tries to merge into existing compatible stacks and
    /// then falls back to empty destination slots.
    ///
    /// @param stack            mutable stack being moved
    /// @param destinationSlots destination slot list
    /// @param reverse          whether the destination order should be reversed
    ///
    /// @return {@code true} when at least part of the stack moved
    ///
    protected final boolean moveToSlots(ItemStack stack, List<Slot> destinationSlots, boolean reverse) {
        if (stack.isEmpty() || destinationSlots.isEmpty()) {
            return false;
        }
        
        boolean changed = false;
        changed |= this.mergeIntoFilledSlots(stack, destinationSlots, reverse);
        changed |= this.fillEmptySlots(stack, destinationSlots, reverse);
        return changed;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (this.quickMovePlan == null || slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        
        Slot clickedSlot = this.slots.get(slotIndex);
        if (!clickedSlot.hasItem() || !clickedSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        
        SLSlotKey sourceKey = this.getSlotKey(clickedSlot);
        if (sourceKey == null) {
            return ItemStack.EMPTY;
        }
        
        ItemStack originalStack = clickedSlot.getItem().copy();
        ItemStack movingStack = clickedSlot.getItem();
        int originalCount = movingStack.getCount();
        
        for (SLQuickMovePlan.RouteTarget routeTarget : this.quickMovePlan.targetsFor(sourceKey)) {
            this.moveToKey(movingStack, routeTarget.target(), routeTarget.reverse());
            if (movingStack.isEmpty()) {
                break;
            }
        }
        
        if (movingStack.getCount() == originalCount) {
            return ItemStack.EMPTY;
        }
        
        if (movingStack.isEmpty()) {
            clickedSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }
        
        int movedItemCount = originalStack.getCount() - movingStack.getCount();
        if (movedItemCount > 0) {
            clickedSlot.onTake(player, originalStack.copyWithCount(movedItemCount));
        }
        
        return originalStack;
    }
    
    ///
    /// Walks the logical menu tree and registers every runtime slot.
    ///
    /// @param node         current logical node
    /// @param inheritedKey key inherited from the nearest parent group
    ///
    private void registerLogicNode(SLMenuNode node, @Nullable SLSlotKey inheritedKey) {
        if (node instanceof SLMenuGroupNode groupNode) {
            for (SLMenuNode child : groupNode.getChildren()) {
                this.registerLogicNode(child, groupNode.getSlotKey());
            }
            return;
        }
        
        if (node instanceof SLMenuSlotNode slotNode) {
            if (inheritedKey == null) {
                throw new IllegalStateException("Slot node was declared outside a keyed group in " + this.getClass().getName());
            }
            this.addLogicalSlot(slotNode.createSlot(), inheritedKey);
            return;
        }
        
        if (node instanceof SLMenuContainerNode containerNode) {
            for (SLMenuNode child : containerNode.getChildren()) {
                this.registerLogicNode(child, inheritedKey);
            }
        }
    }
    
    ///
    /// Tries to merge a stack into already-filled compatible destination slots.
    ///
    /// @param stack            mutable stack being moved
    /// @param destinationSlots destination slots
    /// @param reverse          whether the destination order should be reversed
    ///
    /// @return {@code true} when at least one merge succeeded
    ///
    private boolean mergeIntoFilledSlots(ItemStack stack, List<Slot> destinationSlots, boolean reverse) {
        if (!stack.isStackable()) {
            return false;
        }
        
        boolean changed = false;
        for (int slotIndex = 0; slotIndex < destinationSlots.size(); slotIndex++) {
            Slot destinationSlot = destinationSlots.get(this.resolveDestinationIndex(destinationSlots, slotIndex, reverse));
            ItemStack destinationStack = destinationSlot.getItem();
            
            if (destinationStack.isEmpty()) {
                continue;
            }
            if (!destinationSlot.mayPlace(stack)) {
                continue;
            }
            if (!ItemStack.isSameItemSameComponents(stack, destinationStack)) {
                continue;
            }
            
            int maxStackSize = Math.min(destinationSlot.getMaxStackSize(destinationStack), destinationStack.getMaxStackSize());
            int transferableCount = Math.min(maxStackSize - destinationStack.getCount(), stack.getCount());
            if (transferableCount <= 0) {
                continue;
            }
            
            destinationStack.grow(transferableCount);
            stack.shrink(transferableCount);
            destinationSlot.setChanged();
            changed = true;
            
            if (stack.isEmpty()) {
                return true;
            }
        }
        
        return changed;
    }
    
    ///
    /// Tries to place a stack into empty destination slots.
    ///
    /// @param stack            mutable stack being moved
    /// @param destinationSlots destination slots
    /// @param reverse          whether the destination order should be reversed
    ///
    /// @return {@code true} when at least one placement succeeded
    ///
    private boolean fillEmptySlots(ItemStack stack, List<Slot> destinationSlots, boolean reverse) {
        boolean changed = false;
        for (int slotIndex = 0; slotIndex < destinationSlots.size(); slotIndex++) {
            Slot destinationSlot = destinationSlots.get(this.resolveDestinationIndex(destinationSlots, slotIndex, reverse));
            if (destinationSlot.hasItem() || !destinationSlot.mayPlace(stack)) {
                continue;
            }
            
            int movedItemCount = Math.min(destinationSlot.getMaxStackSize(stack), stack.getCount());
            ItemStack movedStack = stack.copyWithCount(movedItemCount);
            destinationSlot.setByPlayer(movedStack);
            destinationSlot.setChanged();
            stack.shrink(movedItemCount);
            changed = true;
            
            if (stack.isEmpty()) {
                return true;
            }
        }
        
        return changed;
    }
    
    ///
    /// Resolves one destination index while honoring reverse-order traversal.
    ///
    /// @param destinationSlots destination slot list
    /// @param slotIndex        forward loop index
    /// @param reverse          whether the list should be traversed backwards
    ///
    /// @return resolved index inside the destination list
    ///
    private int resolveDestinationIndex(List<Slot> destinationSlots, int slotIndex, boolean reverse) {
        if (reverse) {
            return (destinationSlots.size() - 1) - slotIndex;
        }
        return slotIndex;
    }
}
