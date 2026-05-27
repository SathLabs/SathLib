package dev.satherov.sathlib.common.menu.logic;

import dev.satherov.sathlib.common.menu.slot.SLContainerSlot;
import dev.satherov.sathlib.common.menu.slot.SLHotbarSlot;
import dev.satherov.sathlib.common.menu.slot.SLPlayerInventorySlot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

///
/// Fluent helpers for building logical menu trees.
///
/// The menu DSL describes what slots exist and how they are grouped, but it
/// never stores screen coordinates. Client screens decide placement entirely
/// from retained UI nodes.
///
public final class SLMenus {
    
    private SLMenus() { }
    
    ///
    /// Creates an empty root builder.
    ///
    /// @return new root builder
    ///
    public static RootBuilder root() {
        return new RootBuilder();
    }
    
    ///
    /// Creates and configures a root node in one call.
    ///
    /// @param configure builder callback
    ///
    /// @return built root node
    ///
    public static SLMenuContainerNode root(Consumer<RootBuilder> configure) {
        Objects.requireNonNull(configure);
        RootBuilder builder = SLMenus.root();
        configure.accept(builder);
        return builder.build();
    }
    
    ///
    /// Creates an empty group builder.
    ///
    /// @param slotKey key assigned to descendant slots
    ///
    /// @return new group builder
    ///
    public static GroupBuilder group(SLSlotKey slotKey) {
        return new GroupBuilder(slotKey);
    }
    
    ///
    /// Creates and configures a group node in one call.
    ///
    /// @param slotKey   key assigned to descendant slots
    /// @param configure builder callback
    ///
    /// @return built group node
    ///
    public static SLMenuGroupNode group(SLSlotKey slotKey, Consumer<GroupBuilder> configure) {
        Objects.requireNonNull(configure);
        GroupBuilder builder = SLMenus.group(slotKey);
        configure.accept(builder);
        return builder.build();
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
    /// @param container backing container
    /// @param slotIndex backing slot index
    ///
    /// @return new slot node
    ///
    public static SLMenuSlotNode slot(Container container, int slotIndex) {
        return SLMenus.slot(() -> new SLContainerSlot(container, slotIndex));
    }
    
    ///
    /// Creates a logical group for a linear slot range.
    ///
    /// @param slotKey   key assigned to the range
    /// @param container backing container
    /// @param firstSlot first backing slot index
    /// @param slotCount slot count in the range
    ///
    /// @return built group node
    ///
    public static SLMenuGroupNode slots(SLSlotKey slotKey, Container container, int firstSlot, int slotCount) {
        SLMenuGroupNode group = new SLMenuGroupNode(slotKey);
        for (int slotOffset = 0; slotOffset < slotCount; slotOffset++) {
            group.addChild(SLMenus.slot(container, firstSlot + slotOffset));
        }
        return group;
    }
    
    ///
    /// Creates a logical group for a rectangular slot range.
    ///
    /// @param slotKey   key assigned to the range
    /// @param container backing container
    /// @param firstSlot first backing slot index
    /// @param columns   grid column count
    /// @param rows      grid row count
    ///
    /// @return built group node
    ///
    public static SLMenuGroupNode grid(SLSlotKey slotKey, Container container, int firstSlot, int columns, int rows) {
        return SLMenus.slots(slotKey, container, firstSlot, columns * rows);
    }
    
    ///
    /// Creates the standard 3x9 player inventory group.
    ///
    /// @param inventory player inventory
    ///
    /// @return built player inventory group
    ///
    public static SLMenuGroupNode playerInventory(Inventory inventory) {
        SLMenuGroupNode group = new SLMenuGroupNode(SLSlotKeys.PLAYER_INVENTORY);
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
    /// @return built hotbar group
    ///
    public static SLMenuGroupNode hotbar(Inventory inventory) {
        SLMenuGroupNode group = new SLMenuGroupNode(SLSlotKeys.PLAYER_HOTBAR);
        for (int slotIndex = 0; slotIndex < Inventory.getSelectionSize(); slotIndex++) {
            int hotbarSlotIndex = slotIndex;
            group.addChild(SLMenus.slot(() -> new SLHotbarSlot(inventory, hotbarSlotIndex)));
        }
        return group;
    }
    
    ///
    /// Shared container-builder base for logical menu trees.
    ///
    /// @param <N> runtime node type produced by the builder
    /// @param <B> concrete builder subtype used for fluent chaining
    ///
    public abstract static class ContainerBuilder<N extends SLMenuContainerNode, B extends ContainerBuilder<N, B>> {
        
        private final List<SLMenuNode> children = new ArrayList<>();
        
        ///
        /// Creates an empty builder.
        ///
        protected ContainerBuilder() { }
        
        ///
        /// Returns this builder as its concrete subtype.
        ///
        /// @return this builder
        ///
        @SuppressWarnings("unchecked")
        protected final B self() {
            return (B) this;
        }
        
        ///
        /// Adds a prebuilt child node.
        ///
        /// @param child child node to append
        ///
        /// @return this builder
        ///
        public final B child(SLMenuNode child) {
            this.children.add(Objects.requireNonNull(child));
            return this.self();
        }
        
        ///
        /// Adds a single custom slot node.
        ///
        /// @param slotFactory runtime slot factory
        ///
        /// @return this builder
        ///
        public final B slot(SLMenuSlotFactory slotFactory) {
            return this.child(SLMenus.slot(slotFactory));
        }
        
        ///
        /// Adds a single container-backed slot node.
        ///
        /// @param container backing container
        /// @param slotIndex backing slot index
        ///
        /// @return this builder
        ///
        public final B slot(Container container, int slotIndex) {
            return this.child(SLMenus.slot(container, slotIndex));
        }
        
        ///
        /// Adds a keyed linear slot group.
        ///
        /// @param slotKey   key assigned to the range
        /// @param container backing container
        /// @param firstSlot first backing slot index
        /// @param slotCount slot count in the range
        ///
        /// @return this builder
        ///
        public final B slots(SLSlotKey slotKey, Container container, int firstSlot, int slotCount) {
            return this.child(SLMenus.slots(slotKey, container, firstSlot, slotCount));
        }
        
        ///
        /// Adds a keyed grid slot group.
        ///
        /// @param slotKey   key assigned to the range
        /// @param container backing container
        /// @param firstSlot first backing slot index
        /// @param columns   grid column count
        /// @param rows      grid row count
        ///
        /// @return this builder
        ///
        public final B grid(SLSlotKey slotKey, Container container, int firstSlot, int columns, int rows) {
            return this.child(SLMenus.grid(slotKey, container, firstSlot, columns, rows));
        }
        
        ///
        /// Adds a configured keyed child group.
        ///
        /// @param slotKey   key assigned to the child group
        /// @param configure child-group builder callback
        ///
        /// @return this builder
        ///
        public final B group(SLSlotKey slotKey, Consumer<GroupBuilder> configure) {
            return this.child(SLMenus.group(slotKey, configure));
        }
        
        ///
        /// Adds the standard player inventory group.
        ///
        /// @param inventory player inventory
        ///
        /// @return this builder
        ///
        public final B playerInventory(Inventory inventory) {
            return this.child(SLMenus.playerInventory(inventory));
        }
        
        ///
        /// Adds the standard hotbar group.
        ///
        /// @param inventory player inventory
        ///
        /// @return this builder
        ///
        public final B hotbar(Inventory inventory) {
            return this.child(SLMenus.hotbar(inventory));
        }
        
        ///
        /// Applies the collected children to a runtime node.
        ///
        /// @param node runtime node to populate
        ///
        protected final void applyChildren(N node) {
            for (SLMenuNode child : this.children) {
                node.addChild(child);
            }
        }
        
        ///
        /// Builds the runtime logical node.
        ///
        /// @return built runtime node
        ///
        public abstract N build();
    }
    
    ///
    /// Builder for the root logical node.
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
    /// Builder for keyed slot groups.
    ///
    public static final class GroupBuilder extends ContainerBuilder<SLMenuGroupNode, GroupBuilder> {
        
        private final SLSlotKey slotKey;
        
        private GroupBuilder(SLSlotKey slotKey) {
            this.slotKey = Objects.requireNonNull(slotKey);
        }
        
        @Override
        public SLMenuGroupNode build() {
            SLMenuGroupNode node = new SLMenuGroupNode(this.slotKey);
            this.applyChildren(node);
            return node;
        }
    }
}
