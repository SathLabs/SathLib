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
