package dev.satherov.sathlib.common.menu.logic;

import dev.satherov.sathlib.common.menu.slot.SLContainerSlot;
import dev.satherov.sathlib.common.menu.slot.SLHotbarSlot;
import dev.satherov.sathlib.common.menu.slot.SLPlayerInventorySlot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

///
/// Fluent helpers for building menu logic trees.
///
/// The menu DSL describes which slots exist and how they are grouped, but it
/// never assigns screen coordinates. Any vanilla constructor coordinates remain
/// an internal slot implementation detail and are never part of the menu API.
///
/// - build retained menu logic trees
/// - provide common grouped slot helpers
/// - keep menu constructors readable
///
public final class SLMenus {
    
    private SLMenus() { }
    
    ///
    /// Creates a root container builder.
    ///
    /// @return new root builder
    ///
    public static RootBuilder root() {
        return new RootBuilder();
    }
    
    ///
    /// Creates a semantic group builder.
    ///
    /// @param semantic semantic role assigned to descendant slots
    ///
    /// @return new group builder
    ///
    public static GroupBuilder group(SLSlotSemantic semantic) {
        return new GroupBuilder(semantic);
    }
    
    ///
    /// Creates a slot node from a custom slot factory.
    ///
    /// @param slotFactory runtime slot factory
    ///
    /// @return new slot node
    ///
    public static SLMenuSlotNode slot(SLMenuSlotFactory slotFactory) {
        return new SLMenuSlotNode(slotFactory);
    }
    
    ///
    /// Creates a normal container-backed slot node.
    ///
    /// Screen placement stays entirely on the client side.
    ///
    /// @param container backing container
    /// @param slotIndex container slot index
    ///
    /// @return new slot node
    ///
    public static SLMenuSlotNode slot(Container container, int slotIndex) {
        return SLMenus.slot(() -> new SLContainerSlot(container, slotIndex));
    }
    
    ///
    /// Creates a semantic group for a linear container slot range.
    ///
    /// @param semantic  semantic role of the group
    /// @param container backing container
    /// @param firstSlot first container slot index
    /// @param slotCount amount of slots
    ///
    /// @return new group node
    ///
    public static SLMenuGroupNode slots(SLSlotSemantic semantic, Container container, int firstSlot, int slotCount) {
        SLMenuGroupNode group = new SLMenuGroupNode(semantic);
        for (int slotOffset = 0; slotOffset < slotCount; slotOffset++) {
            group.addChild(SLMenus.slot(container, firstSlot + slotOffset));
        }
        return group;
    }
    
    ///
    /// Creates a semantic group for a grid of container slots.
    ///
    /// @param semantic  semantic role of the group
    /// @param container backing container
    /// @param firstSlot first container slot index
    /// @param columns   column count
    /// @param rows      row count
    ///
    /// @return new group node
    ///
    public static SLMenuGroupNode grid(SLSlotSemantic semantic, Container container, int firstSlot, int columns, int rows) {
        return SLMenus.slots(semantic, container, firstSlot, columns * rows);
    }
    
    ///
    /// Creates the standard 3x9 player inventory group.
    ///
    /// @param inventory player inventory
    ///
    /// @return player inventory group
    ///
    public static SLMenuGroupNode playerInventory(Inventory inventory) {
        SLMenuGroupNode group = new SLMenuGroupNode(SLSlotSemantics.PLAYER_INVENTORY);
        for (int slotIndex = Inventory.getSelectionSize(); slotIndex < Inventory.getSelectionSize() + 27; slotIndex++) {
            int inventorySlotIndex = slotIndex;
            group.addChild(SLMenus.slot(() -> new SLPlayerInventorySlot(inventory, inventorySlotIndex)));
        }
        return group;
    }
    
    ///
    /// Creates the standard 9-slot hotbar group.
    ///
    /// @param inventory player inventory
    ///
    /// @return hotbar group
    ///
    public static SLMenuGroupNode hotbar(Inventory inventory) {
        SLMenuGroupNode group = new SLMenuGroupNode(SLSlotSemantics.PLAYER_HOTBAR);
        for (int slotIndex = 0; slotIndex < Inventory.getSelectionSize(); slotIndex++) {
            int hotbarSlotIndex = slotIndex;
            group.addChild(SLMenus.slot(() -> new SLHotbarSlot(inventory, hotbarSlotIndex)));
        }
        return group;
    }
    
    ///
    /// Shared container builder base for menu logic trees.
    ///
    /// @param <N> runtime container node type produced by the builder
    /// @param <B> concrete builder type used for fluent chaining
    ///
    public abstract static class ContainerBuilder<N extends SLMenuContainerNode, B extends ContainerBuilder<N, B>> {
        
        private final java.util.List<SLMenuNode> children = new java.util.ArrayList<>();
        
        ///
        /// Creates an empty container builder.
        ///
        protected ContainerBuilder() { }
        
        ///
        /// Returns the concrete builder type.
        ///
        /// @return concrete builder
        ///
        @SuppressWarnings("unchecked")
        protected final B self() {
            return (B) this;
        }
        
        ///
        /// Adds a child node to this builder.
        ///
        /// @param child child node
        ///
        /// @return this builder
        ///
        public final B child(SLMenuNode child) {
            this.children.add(child);
            return this.self();
        }
        
        ///
        /// Applies the collected children to a runtime container node.
        ///
        /// @param node runtime container node to populate
        ///
        protected final void applyChildren(N node) {
            for (SLMenuNode child : this.children) {
                node.addChild(child);
            }
        }
        
        ///
        /// Builds the runtime menu logic node.
        ///
        /// @return built node
        ///
        public abstract N build();
    }
    
    ///
    /// Builder for the root container node.
    ///
    public static final class RootBuilder extends ContainerBuilder<SLMenuContainerNode, RootBuilder> {
        
        private RootBuilder() { }
        
        @Override
        public SLMenuContainerNode build() {
            SLMenuContainerNode node = new SLMenuContainerNode();
            this.applyChildren(node);
            return node;
        }
    }
    
    ///
    /// Builder for semantic group nodes.
    ///
    public static final class GroupBuilder extends ContainerBuilder<SLMenuGroupNode, GroupBuilder> {
        
        private final SLSlotSemantic semantic;
        
        private GroupBuilder(SLSlotSemantic semantic) {
            this.semantic = semantic;
        }
        
        @Override
        public SLMenuGroupNode build() {
            SLMenuGroupNode node = new SLMenuGroupNode(this.semantic);
            this.applyChildren(node);
            return node;
        }
    }
}
