package dev.satherov.sathlib.common.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableMap;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

///
/// Container for a collection of block item properties.
///
/// @param <B> the block class
/// @param <I> the item class
///
@Getter
@NothingNull
@SuppressWarnings("doclint:missing")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockItemPropertyContainer<B, I> implements Iterable<BlockItemProperty<?>> {
    
    private final Class<B> blockClass;
    private final Class<I> itemClass;
    private final Map<Identifier, BlockItemProperty<?>> properties;
    
    ///
    /// Creates a new container builder
    ///
    /// @param blockClass the block class this container applies to
    /// @param itemClass  the item class this container applies to
    /// @param <B>        the block class
    /// @param <I>        the item class
    ///
    /// @return the container builder
    ///
    public static <B, I> Builder<B, I> builder(Class<B> blockClass, Class<I> itemClass) {
        return new Builder<>(blockClass, itemClass);
    }
    
    // ===== ===== ===== ===== APPLICATION ===== ===== ===== =====
    
    ///
    /// Applies all properties in this container from the BlockState to the ItemStack
    ///
    /// @param stack the ItemStack to apply the properties to
    /// @param state the BlockState to apply the properties from
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder applyToItem(ItemStack stack, BlockState state) {
        return this.applyToItem(stack, state, null);
    }
    
    ///
    /// Applies all properties in this container from the BlockState or BlockEntity to the ItemStack
    ///
    /// @param stack  the ItemStack to apply the properties to
    /// @param state  the BlockState to apply the properties from
    /// @param entity the BlockEntity to apply the properties from
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder applyToItem(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        PropertyItemHolder item = new PropertyItemHolder(stack);
        if (!this.supports(stack, state)) return item;
        for (BlockItemProperty<?> property : this) item = property.applyToItem(item.stack(), state, entity);
        return item;
    }
    
    ///
    /// Applies all properties in this container from the ItemStack to the BlockState
    ///
    /// @param stack the ItemStack to apply the properties from
    /// @param state the BlockState to apply the properties to
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder applyToBlock(ItemStack stack, BlockState state) {
        return this.applyToBlock(stack, state, null);
    }
    
    ///
    /// Applies all properties in this container from the ItemStack to the BlockState or BlockEntity
    ///
    /// @param stack  the ItemStack to apply the properties from
    /// @param state  the BlockState to apply the properties to
    /// @param entity the BlockEntity to apply the properties to
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder applyToBlock(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        if (!this.supports(stack, state)) return block;
        for (BlockItemProperty<?> property : this) block = property.applyToBlock(stack, block.state(), block.blockEntity());
        return block;
    }
    
    // ===== ===== ===== ===== MATCHING ===== ===== ===== =====
    
    ///
    /// Checks if all properties in this container match in the two given ItemStacks
    ///
    /// @param stack the first ItemStack
    /// @param other the second ItemStack
    /// @param state the BlockState supplied
    ///
    /// @return `true` if all properties match, `false` otherwise
    ///
    public boolean matchItems(ItemStack stack, ItemStack other, BlockState state) {
        return this.matchItems(stack, other, state, null);
    }
    
    ///
    /// Checks if all properties in this container match in the two given ItemStacks
    ///
    /// @param stack  the first ItemStack
    /// @param other  the second ItemStack
    /// @param state  the BlockState supplied
    /// @param entity the BlockEntity supplied
    ///
    /// @return `true` if all properties match, `false` otherwise
    ///
    public boolean matchItems(ItemStack stack, ItemStack other, BlockState state, @Nullable BlockEntity entity) {
        if (!(this.supports(stack) && this.supports(other) && this.supports(state))) return false;
        for (BlockItemProperty<?> property : this) {
            if (!property.matchItems(stack, other, state, entity)) {
                return false;
            }
        }
        return true;
    }
    
    ///
    /// Checks if all properties in this container match in the two given BlockStates
    ///
    /// @param stack the ItemStack supplied
    /// @param state the first BlockState
    /// @param other the second BlockState
    ///
    /// @return `true` if all properties match, `false` otherwise
    ///
    public boolean matchBlocks(ItemStack stack, BlockState state, BlockState other) {
        return this.matchBlocks(stack, state, null, other, null);
    }
    
    ///
    /// Checks if all properties in this container match in the two given BlockStates and BlockEntities
    ///
    /// @param stack       the ItemStack supplied
    /// @param state       the first BlockState
    /// @param entity      the first BlockEntity
    /// @param other       the second BlockState
    /// @param otherEntity the second BlockEntity
    ///
    /// @return `true` if all properties match, `false` otherwise
    ///
    public boolean matchBlocks(ItemStack stack, BlockState state, @Nullable BlockEntity entity, BlockState other, @Nullable BlockEntity otherEntity) {
        if (!(this.supports(stack) && this.supports(state) && this.supports(other))) return false;
        for (BlockItemProperty<?> property : this) {
            if (!property.matchBlocks(stack, state, entity, other, otherEntity)) {
                return false;
            }
        }
        return true;
    }
    
    ///
    /// Checks if all properties in this container match in the given ItemStack and BlockState
    ///
    /// @param stack the ItemStack to check
    /// @param state the BlockState to check
    ///
    /// @return `true` if all properties match, `false` otherwise
    ///
    public boolean matches(ItemStack stack, BlockState state) {
        return this.matches(stack, state, null);
    }
    
    ///
    /// Checks if all properties in this container match in the given ItemStack and BlockState
    ///
    /// @param stack  the ItemStack to check
    /// @param state  the BlockState to check
    /// @param entity the BlockEntity to check
    ///
    /// @return `true` if all properties match, `false` otherwise
    ///
    public boolean matches(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        if (!(this.supports(stack) && this.supports(state))) return false;
        for (BlockItemProperty<?> property : this) {
            if (!property.match(stack, state, entity)) {
                return false;
            }
        }
        return true;
    }
    
    // ===== ===== ===== ===== ITERATION ===== ===== ===== =====
    
    @Override
    public Iterator<BlockItemProperty<?>> iterator() {
        return this.properties.values().iterator();
    }
    
    ///
    /// Unmodifiable copy of the properties in this container
    ///
    /// @return unmodifiable copy of the properties in this container
    ///
    public List<BlockItemProperty<?>> getProperties() {
        return List.copyOf(this.properties.values());
    }
    
    ///
    /// Gets a property by its identifier
    ///
    /// @param identifier the identifier of the property
    ///
    /// @return the property with the given identifier, or `null` if not found
    ///
    public @Nullable BlockItemProperty<?> getProperty(Identifier identifier) {
        return this.properties.get(identifier);
    }
    
    // ===== ===== ===== ===== VALIDITY ===== ===== ===== =====
    
    ///
    /// Checks if the given ItemStack and BlockState are supported by this container
    ///
    /// @param stack the ItemStack to check
    /// @param state the BlockState to check
    ///
    /// @return `true` if the ItemStack and BlockState are supported, `false` otherwise
    ///
    private boolean supports(ItemStack stack, BlockState state) {
        return this.supports(stack) && this.supports(state);
    }
    
    ///
    /// Checks if the given ItemStack is supported by this container
    ///
    /// @param stack the ItemStack to check
    ///
    /// @return `true` if the ItemStack is supported, `false` otherwise
    ///
    private boolean supports(ItemStack stack) {
        return this.itemClass.isInstance(stack.getItem());
    }
    
    ///
    /// Checks if the given BlockState is supported by this container
    ///
    /// @param state the BlockState to check
    ///
    /// @return `true` if the BlockState is supported, `false` otherwise
    ///
    private boolean supports(BlockState state) {
        return this.blockClass.isInstance(state.getBlock());
    }
    
    ///
    /// Builder for {@link BlockItemPropertyContainer}
    ///
    /// @param <B> the block class
    /// @param <I> the item class
    ///
    @RequiredArgsConstructor
    public static final class Builder<B, I> {
        
        private final Class<B> blockClass;
        private final Class<I> itemClass;
        private final ImmutableMap.Builder<Identifier, BlockItemProperty<?>> properties = new ImmutableMap.Builder<>();
        
        ///
        /// Adds a block item property to the container.
        ///
        /// @param property the property to add
        ///
        /// @return this builder
        ///
        public Builder<B, I> property(BlockItemProperty<?> property) {
            this.properties.put(property.getIdentifier(), property);
            return this;
        }
        
        ///
        /// Builds the container.
        ///
        /// @return the built container
        ///
        public BlockItemPropertyContainer<B, I> build() {
            return new BlockItemPropertyContainer<>(this.blockClass, this.itemClass, this.properties.build());
        }
    }
}
