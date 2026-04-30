package dev.satherov.sathlib.common.properties;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

///
/// Property bridge between one block type, item type, and block entity type.
///
/// @param <B> supported block type
/// @param <I> supported item type
/// @param <E> supported block entity type
///
@Slf4j
@NothingNull
@SuppressWarnings("doclint:missing")
public class SLPropertyContainer<B extends Block, I extends Item, E extends BlockEntity> implements Iterable<SLProperty<?, E>> {
    
    protected final Class<B> block;
    protected final Class<I> item;
    protected final Class<E> blockEntity;
    protected final Map<Identifier, SLProperty<?, E>> properties;
    
    @Builder
    private SLPropertyContainer(Class<B> block, Class<I> item, Class<E> blockEntity, Map<Identifier, SLProperty<?, E>> properties) {
        this.block = block;
        this.item = item;
        this.blockEntity = blockEntity;
        this.properties = properties;
    }
    
    ///
    /// Starts a new builder. Requires the class of the block and item that should interchange values.
    /// Using {@code Block.class} and {@code Item.class} disabled the check for matching classes but may lead to issues
    ///
    /// @param block       Class or super class of the block
    /// @param item        Class or super class of the item
    /// @param blockEntity Class or super class of the block entity
    /// @param <B>         Supported block type
    /// @param <I>         Supported item type
    /// @param <E>         Supported block entity type
    ///
    /// @return new property container builder
    ///
    public static <B extends Block, I extends Item, E extends BlockEntity> SLPropertyContainerBuilder<B, I, E> builder(Class<B> block, Class<I> item, Class<E> blockEntity) {
        return new SLPropertyContainerBuilder<B, I, E>().block(block).item(item).blockEntity(blockEntity).properties(new LinkedHashMap<>());
    }
    
    ///
    /// Gets a property from the container by its id
    ///
    /// @param id Identifier of the property
    ///
    /// @return the property
    ///
    public @Nullable SLProperty<?, E> get(Identifier id) {
        return this.properties.get(id);
    }
    
    ///
    /// Extracts all properties from the {@link BlockState} and applies them to the {@link ItemStack}
    ///
    /// @param state The BlockState to extract the properties from
    /// @param stack The ItemStack to apply the properties to
    ///
    public void updateFromState(BlockState state, ItemStack stack) {
        if (this.validate(stack, state, null)) return;
        for (SLProperty<?, E> property : this.properties.values()) {
            property.updateFromState(state, stack);
        }
    }
    
    ///
    /// Extracts all properties from the {@link BlockState} and applies them to the {@link BlockEntity}
    ///
    /// @param state  The BlockState to extract the properties from
    /// @param entity The BlockEntity to apply the properties to
    ///
    public void updateFromState(BlockState state, E entity) {
        if (this.validate(null, state, entity)) return;
        for (SLProperty<?, E> property : this.properties.values()) {
            property.updateFromState(state, entity);
        }
    }
    
    ///
    /// Extracts all properties from the {@link ItemStack} and applies them to the {@link BlockState}
    ///
    /// @param stack The ItemStack to extract the properties from
    /// @param state The BlockState to apply the properties to
    ///
    /// @return the BlockState after modification
    ///
    public BlockState updateFromStack(ItemStack stack, BlockState state) {
        if (this.validate(stack, state, null)) return state;
        for (SLProperty<?, E> property : this.properties.values()) {
            state = property.updateFromStack(stack, state);
        }
        return state;
    }
    
    ///
    /// Extracts all properties from the {@link ItemStack} and applies them to the {@link BlockEntity}
    ///
    /// @param stack  The BlockState to extract the properties from
    /// @param entity The BlockEntity to apply the properties to
    ///
    public void updateFromStack(ItemStack stack, E entity) {
        if (this.validate(stack, null, entity)) return;
        for (SLProperty<?, E> property : this.properties.values()) {
            property.updateFromStack(stack, entity);
        }
    }
    
    ///
    /// Extracts all properties from the {@link BlockEntity} and applies them to the {@link BlockState}
    ///
    /// @param entity The ItemStack to extract the properties from
    /// @param state  The BlockState to apply the properties to
    ///
    /// @return the BlockState after modification
    ///
    public BlockState updateFromBlockEntity(E entity, BlockState state) {
        if (this.validate(null, state, entity)) return state;
        for (SLProperty<?, E> property : this.properties.values()) {
            state = property.updateFromBlockEntity(entity, state);
        }
        return state;
    }
    
    ///
    /// Extracts all properties from the {@link BlockEntity} and applies them to the {@link ItemStack}
    ///
    /// @param entity The BlockEntity to extract the properties from
    /// @param stack  The ItemStack to apply the properties to
    ///
    public void updateFromBlockEntity(E entity, ItemStack stack) {
        if (this.validate(stack, null, entity)) return;
        for (SLProperty<?, E> property : this.properties.values()) {
            property.updateFromBlockEntity(entity, stack);
        }
    }
    
    ///
    /// Updates the given BlockState from the given ItemStack or BlockEntity.
    ///
    /// @param state  block state to update
    /// @param stack  item stack to get value from
    /// @param entity block entity to get value from if the item stack does not contain a value
    ///
    public BlockState updateState(BlockState state, ItemStack stack, E entity) {
        if (this.validate(stack, state, entity)) return state;
        for (SLProperty<?, E> property : this.properties.values()) {
            state = property.updateState(state, stack, entity);
        }
        return state;
    }
    
    ///
    /// Updates the given ItemStack from the given BlockState or BlockEntity.
    ///
    /// @param stack  item stack to update
    /// @param state  block state to get value from
    /// @param entity block entity to get value from if the block state does not contain a value
    ///
    public void updateStack(ItemStack stack, BlockState state, E entity) {
        if (this.validate(stack, state, entity)) return;
        for (SLProperty<?, E> property : this.properties.values()) {
            property.updateStack(stack, state, entity);
        }
    }
    
    ///
    /// Updates the given BlockEntity from the given BlockState or ItemStack.
    ///
    /// @param entity block entity to update
    /// @param state  block state to get value from
    /// @param stack  item stack to get value from if the block state does not contain a value
    ///
    public void updateEntity(E entity, BlockState state, ItemStack stack) {
        if (this.validate(stack, state, entity)) return;
        for (SLProperty<?, E> property : this.properties.values()) {
            property.updateEntity(entity, state, stack);
        }
    }

    ///
    /// Checks if the given BlockState matches the given ItemStack or BlockEntity.
    ///
    /// @param state  the BlockState to compare
    /// @param stack  the ItemStack to compare against
    /// @param entity the BlockEntity to compare against
    ///
    /// @return `true` if every property of the BlockState matches either the ItemStack or the BlockEntity, `false` otherwise
    ///
    public boolean matches(BlockState state, ItemStack stack, E entity) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(state, stack, entity)) return false;
        }
        return true;
    }

    ///
    /// Checks if the given ItemStack matches the given BlockState or BlockEntity.
    ///
    /// @param stack  the ItemStack to compare
    /// @param state  the BlockState to compare against
    /// @param entity the BlockEntity to compare against
    ///
    /// @return `true` if every property of the ItemStack matches either the BlockState or the BlockEntity, `false` otherwise
    ///
    public boolean matches(ItemStack stack, BlockState state, E entity) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(stack, state, entity)) return false;
        }
        return true;
    }

    ///
    /// Checks if the given BlockEntity matches the given BlockState or ItemStack.
    ///
    /// @param entity the BlockEntity to compare
    /// @param state  the BlockState to compare against
    /// @param stack  the ItemStack to compare against
    ///
    /// @return `true` if every property of the BlockEntity matches either the BlockState or the ItemStack, `false` otherwise
    ///
    public boolean matches(E entity, BlockState state, ItemStack stack) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(entity, state, stack)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the two given BlockStates have equal values.
    ///
    /// @param state the first BlockState
    /// @param other the second BlockState
    ///
    /// @return `true` if the two BlockStates have the same value, `false` otherwise
    ///
    public boolean matches(BlockState state,  BlockState other) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(state, other)) return false;
        }
        return true;
    }

    ///
    /// Checks if the two given BlockStates have equal values or if either side is unavailable.
    ///
    /// @param state the first BlockState
    /// @param other the second BlockState
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable BlockState state, @Nullable BlockState other) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(state, other)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the BlockState and ItemStack have equal values.
    ///
    /// @param state the BlockState
    /// @param stack the ItemStack
    ///
    /// @return `true` if the BlockState and ItemStack have the same value, `false` otherwise
    ///
    public boolean matches(BlockState state, ItemStack stack) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(state, stack)) return false;
        }
        return true;
    }

    ///
    /// Checks if the BlockState and ItemStack have equal values or if either side is unavailable.
    ///
    /// @param state the BlockState
    /// @param stack the ItemStack
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable BlockState state, @Nullable ItemStack stack) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(state, stack)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the BlockState and BlockEntity have equal values.
    ///
    /// @param state the BlockState
    /// @param entity the BlockEntity
    ///
    /// @return `true` if the BlockState and BlockEntity have the same value, `false` otherwise
    ///
    public boolean matches(BlockState state, E entity) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(state, entity)) return false;
        }
        return true;
    }

    ///
    /// Checks if the BlockState and BlockEntity have equal values or if either side is unavailable.
    ///
    /// @param state the BlockState
    /// @param entity the BlockEntity
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable BlockState state, @Nullable E entity) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(state, entity)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the two given ItemStacks have equal values.
    ///
    /// @param stack the first ItemStack
    /// @param other the second ItemStack
    ///
    /// @return `true` if the two ItemStacks have the same value, `false` otherwise
    ///
    public boolean matches(ItemStack stack, ItemStack other) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(stack, other)) return false;
        }
        return true;
    }

    ///
    /// Checks if the two given ItemStacks have equal values or if either side is unavailable.
    ///
    /// @param stack the first ItemStack
    /// @param other the second ItemStack
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable ItemStack stack, @Nullable ItemStack other) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(stack, other)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the ItemStack and BlockState have equal values.
    ///
    /// @param stack the ItemStack
    /// @param state the BlockState
    ///
    /// @return `true` if the ItemStack and BlockState have the same value, `false` otherwise
    ///
    public boolean matches(ItemStack stack, BlockState state) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(stack, state)) return false;
        }
        return true;
    }

    ///
    /// Checks if the ItemStack and BlockState have equal values or if either side is unavailable.
    ///
    /// @param stack the ItemStack
    /// @param state the BlockState
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable ItemStack stack, @Nullable BlockState state) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(stack, state)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the ItemStack and BlockEntity have equal values.
    ///
    /// @param stack the ItemStack
    /// @param entity the BlockEntity
    ///
    /// @return `true` if the ItemStack and BlockEntity have the same value, `false` otherwise
    ///
    public boolean matches(ItemStack stack, E entity) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(stack, entity)) return false;
        }
        return true;
    }

    ///
    /// Checks if the ItemStack and BlockEntity have equal values or if either side is unavailable.
    ///
    /// @param stack the ItemStack
    /// @param entity the BlockEntity
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable ItemStack stack, @Nullable E entity) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(stack, entity)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the two given BlockEntities have equal values.
    ///
    /// @param entity the first BlockEntity
    /// @param other  the second BlockEntity
    ///
    /// @return `true` if the two BlockEntities have the same value, `false` otherwise
    ///
    public boolean matches(E entity, E other) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(entity, other)) return false;
        }
        return true;
    }

    ///
    /// Checks if the two given BlockEntities have equal values or if either side is unavailable.
    ///
    /// @param entity the first BlockEntity
    /// @param other  the second BlockEntity
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable E entity, @Nullable E other) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(entity, other)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the BlockEntity and BlockState have equal values.
    ///
    /// @param entity the BlockEntity
    /// @param state the BlockState
    ///
    /// @return `true` if the BlockEntity and BlockState have the same value, `false` otherwise
    ///
    public boolean matches(E entity, BlockState state) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(entity, state)) return false;
        }
        return true;
    }

    ///
    /// Checks if the BlockEntity and BlockState have equal values or if either side is unavailable.
    ///
    /// @param entity the BlockEntity
    /// @param state the BlockState
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable E entity, @Nullable BlockState state) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(entity, state)) return false;
        }
        return true;
    }
    
    ///
    /// Checks if the BlockEntity and ItemStack have equal values.
    ///
    /// @param entity the BlockEntity
    /// @param stack the ItemStack
    ///
    /// @return `true` if the BlockEntity and ItemStack have the same value, `false` otherwise
    ///
    public boolean matches(E entity, ItemStack stack) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matches(entity, stack)) return false;
        }
        return true;
    }

    ///
    /// Checks if the BlockEntity and ItemStack have equal values or if either side is unavailable.
    ///
    /// @param entity the BlockEntity
    /// @param stack the ItemStack
    ///
    /// @return `true` if all properties match or any compared side resolves to `null`
    ///
    public boolean matchOrNull(@Nullable E entity, @Nullable ItemStack stack) {
        for (SLProperty<?, E> property : this.properties.values()) {
            if (!property.matchOrNull(entity, stack)) return false;
        }
        return true;
    }
    
    @Override
    public Iterator<SLProperty<?, E>> iterator() {
        return this.properties.values().iterator();
    }
    
    ///
    /// Builder for {@link SLPropertyContainer}.
    ///
    /// @param <B> supported block type
    /// @param <I> supported item type
    /// @param <E> supported block entity type
    ///
    public static class SLPropertyContainerBuilder<B extends Block, I extends Item, E extends BlockEntity> {
        
        private SLPropertyContainerBuilder() { }
        
        ///
        /// Adds a property to this container
        ///
        /// @param property the property to add
        ///
        /// @return the builder instance
        ///
        public SLPropertyContainerBuilder<B, I, E> property(SLProperty<?, E> property) {
            this.properties.put(property.identifier(), property);
            return this;
        }
    }
    
    ///
    /// Does a basic check to return instantly if the classes don't match so we don't waste time doing potentially broken checks later on
    ///
    private boolean validate(@Nullable ItemStack stack, @Nullable BlockState state, @Nullable E entity) {
        if (stack != null && !this.item.isInstance(stack.getItem())) return true;
        if (state != null && !this.block.isInstance(state.getBlock())) return true;
        return entity != null && !this.blockEntity.isInstance(entity);
    }
}
