package dev.satherov.sathlib.common.menu;

import dev.satherov.sathlib.common.menu.logic.SLMenuContainerNode;
import dev.satherov.sathlib.common.menu.logic.SLMenuGroupNode;
import dev.satherov.sathlib.common.menu.logic.SLMenuNode;
import dev.satherov.sathlib.common.menu.logic.SLMenuSlotNode;
import dev.satherov.sathlib.common.menu.logic.SLQuickMovePlan;
import dev.satherov.sathlib.common.menu.logic.SLSlotSemantic;
import dev.satherov.sathlib.common.menu.sync.SLSyncedFields;
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
/// A concrete menu constructs storage references, calls this base constructor,
/// declares annotated sync fields, and then defines a retained menu-logic tree.
/// The tree decides what slots exist and how they are grouped semantically, but
/// it never decides where those slots appear on the client.
///
/// - bind annotated synced fields automatically
/// - register semantic slot groups for screens and quick-move logic
/// - provide a shared quick-move implementation that works on semantics
///
/// Screens own slot positioning. Menus only own logic, storage access, and
/// transfer behavior.
///
@NothingNull
public abstract class SLMenu extends AbstractContainerMenu {
    
    ///
    /// Player inventory bound to this menu instance.
    ///
    protected final Inventory playerInventory;
    private final Map<SLSlotSemantic, List<Slot>> slotsBySemantic = new LinkedHashMap<>();
    private final Map<Slot, SLSlotSemantic> semanticBySlot = new IdentityHashMap<>();
    
    private @Nullable SLMenuContainerNode root;
    private @Nullable SLQuickMovePlan quickMovePlan;
    
    ///
    /// Creates the menu base and binds all annotated sync fields.
    ///
    /// @param type            registered menu type
    /// @param containerId     vanilla container id
    /// @param playerInventory opening player inventory
    ///
    protected SLMenu(
            MenuType<?> type,
            int containerId,
            Inventory playerInventory
    ) {
        super(type, containerId);
        this.playerInventory = Objects.requireNonNull(playerInventory);
        SLSyncedFields.forEachDataSlot(this, this::addDataSlot);
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
    /// Returns all slots registered for one semantic role.
    ///
    /// The returned list is immutable and preserves the registration order from
    /// the menu logic tree.
    ///
    /// @param semantic semantic role to query
    ///
    /// @return immutable slot list
    ///
    public final List<Slot> getSlots(SLSlotSemantic semantic) {
        List<Slot> slots = this.slotsBySemantic.get(semantic);
        if (slots == null) return List.of();
        return Collections.unmodifiableList(slots);
    }
    
    ///
    /// Returns the semantic role for a runtime slot.
    ///
    /// @param slot runtime slot
    ///
    /// @return semantic role, or {@code null} when unknown
    ///
    public final @Nullable SLSlotSemantic getSlotSemantic(Slot slot) {
        return this.semanticBySlot.get(slot);
    }
    
    ///
    /// Returns the semantic role for a slot index.
    ///
    /// @param slotIndex menu slot index
    ///
    /// @return semantic role, or {@code null} when the index is invalid or not
    /// mapped
    ///
    public final @Nullable SLSlotSemantic getSlotSemantic(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return null;
        return this.getSlotSemantic(this.slots.get(slotIndex));
    }
    
    ///
    /// Returns whether the given semantic belongs to the player side.
    ///
    /// @param semantic semantic to test
    ///
    /// @return {@code true} when the semantic is player-side
    ///
    public final boolean isPlayerSideSemantic(SLSlotSemantic semantic) {
        return semantic.playerSide();
    }
    
    ///
    /// Declares the retained menu-logic tree for this menu.
    ///
    /// This method should be called exactly once from the concrete menu
    /// constructor after all backing storage references have been assigned.
    ///
    /// @param root logic root
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
    /// Menus with special-case transfer logic can still override
    /// {@link #quickMoveStack(Player, int)} directly.
    ///
    /// @param quickMovePlan quick-move route table
    ///
    protected final void setQuickMovePlan(SLQuickMovePlan quickMovePlan) {
        this.quickMovePlan = Objects.requireNonNull(quickMovePlan);
    }
    
    ///
    /// Adds one semantic slot directly.
    ///
    /// This is the low-level escape hatch behind the menu tree. Most menus
    /// should prefer {@link #defineMenu(SLMenuContainerNode)} with
    /// `SLMenus`.
    ///
    /// @param slot     runtime slot instance
    /// @param semantic logical slot role
    ///
    /// @return the added slot
    ///
    protected final Slot addSemanticSlot(Slot slot, SLSlotSemantic semantic) {
        Slot addedSlot = super.addSlot(slot);
        this.semanticBySlot.put(addedSlot, semantic);
        this.slotsBySemantic.computeIfAbsent(semantic, ignored -> new ArrayList<>()).add(addedSlot);
        return addedSlot;
    }
    
    ///
    /// Attempts to move a stack into all slots that share one semantic role.
    ///
    /// @param stack    mutable stack being moved
    /// @param semantic destination semantic
    /// @param reverse  whether the destination order should be reversed
    ///
    /// @return {@code true} when at least part of the stack moved
    ///
    protected final boolean moveToSemantic(ItemStack stack, SLSlotSemantic semantic, boolean reverse) {
        return this.moveToSlots(stack, this.getSlots(semantic), reverse);
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
        if (stack.isEmpty() || destinationSlots.isEmpty()) return false;
        
        boolean changed = false;
        changed |= this.mergeIntoFilledSlots(stack, destinationSlots, reverse);
        changed |= this.fillEmptySlots(stack, destinationSlots, reverse);
        return changed;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (this.quickMovePlan == null) return ItemStack.EMPTY;
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return ItemStack.EMPTY;
        
        Slot clickedSlot = this.slots.get(slotIndex);
        if (!clickedSlot.hasItem() || !clickedSlot.mayPickup(player)) return ItemStack.EMPTY;
        
        SLSlotSemantic sourceSemantic = this.getSlotSemantic(clickedSlot);
        if (sourceSemantic == null) return ItemStack.EMPTY;
        
        ItemStack originalStack = clickedSlot.getItem().copy();
        ItemStack movingStack = clickedSlot.getItem();
        int originalCount = movingStack.getCount();
        
        for (SLQuickMovePlan.RouteTarget routeTarget : this.quickMovePlan.targetsFor(sourceSemantic)) {
            this.moveToSemantic(movingStack, routeTarget.semantic(), routeTarget.reverse());
            if (movingStack.isEmpty()) {
                break;
            }
        }
        
        if (movingStack.getCount() == originalCount) return ItemStack.EMPTY;
        
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
    
    private void registerLogicNode(SLMenuNode node, @Nullable SLSlotSemantic inheritedSemantic) {
        if (node instanceof SLMenuGroupNode groupNode) {
            for (SLMenuNode child : groupNode.getChildren()) {
                this.registerLogicNode(child, groupNode.getSemantic());
            }
            return;
        }
        
        if (node instanceof SLMenuSlotNode slotNode) {
            if (inheritedSemantic == null) {
                throw new IllegalStateException("Slot node was declared outside a semantic group in " + this.getClass().getName());
            }
            this.addSemanticSlot(slotNode.createSlot(), inheritedSemantic);
            return;
        }
        
        if (node instanceof SLMenuContainerNode containerNode) {
            for (SLMenuNode child : containerNode.getChildren()) {
                this.registerLogicNode(child, inheritedSemantic);
            }
        }
    }
    
    private boolean mergeIntoFilledSlots(ItemStack stack, List<Slot> destinationSlots, boolean reverse) {
        if (!stack.isStackable()) return false;
        
        boolean changed = false;
        
        for (int slotIndex = 0; slotIndex < destinationSlots.size(); slotIndex++) {
            Slot destinationSlot = destinationSlots.get(this.resolveDestinationIndex(destinationSlots, slotIndex, reverse));
            ItemStack destinationStack = destinationSlot.getItem();
            
            if (destinationStack.isEmpty()) continue;
            if (!destinationSlot.mayPlace(stack)) continue;
            if (!ItemStack.isSameItemSameComponents(stack, destinationStack)) continue;
            
            int maxStackSize = Math.min(destinationSlot.getMaxStackSize(destinationStack), destinationStack.getMaxStackSize());
            int transferableCount = Math.min(maxStackSize - destinationStack.getCount(), stack.getCount());
            if (transferableCount <= 0) {
                continue;
            }
            
            destinationStack.grow(transferableCount);
            stack.shrink(transferableCount);
            destinationSlot.setChanged();
            changed = true;
            
            if (stack.isEmpty()) return true;
        }
        
        return changed;
    }
    
    private boolean fillEmptySlots(ItemStack stack, List<Slot> destinationSlots, boolean reverse) {
        boolean changed = false;
        
        for (int slotIndex = 0; slotIndex < destinationSlots.size(); slotIndex++) {
            Slot destinationSlot = destinationSlots.get(this.resolveDestinationIndex(destinationSlots, slotIndex, reverse));
            if (destinationSlot.hasItem()) continue;
            if (!destinationSlot.mayPlace(stack)) continue;
            
            int movedItemCount = Math.min(destinationSlot.getMaxStackSize(stack), stack.getCount());
            ItemStack movedStack = stack.copyWithCount(movedItemCount);
            destinationSlot.setByPlayer(movedStack);
            destinationSlot.setChanged();
            stack.shrink(movedItemCount);
            changed = true;
            
            if (stack.isEmpty()) return true;
        }
        
        return changed;
    }
    
    private int resolveDestinationIndex(List<Slot> destinationSlots, int slotIndex, boolean reverse) {
        if (reverse) return (destinationSlots.size() - 1) - slotIndex;
        return slotIndex;
    }
}
