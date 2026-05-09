package dev.satherov.sathlib.common.block;

import lombok.extern.slf4j.Slf4j;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

///
/// Base block with higher-level lifecycle hooks and state registration helpers.
///
@Slf4j
@NothingNull
public class SLBlock extends Block {
    
    private @Nullable StateBuilder pendingStateBuilder;
    
    ///
    /// Creates a block with the given properties.
    ///
    /// @param properties block properties used by the base {@link Block}
    ///
    public SLBlock(Properties properties) {
        super(properties);
        if (this.pendingStateBuilder != null) {
            this.registerDefaultState(this.pendingStateBuilder.applyDefaults(this.defaultBlockState()));
            this.pendingStateBuilder = null;
        }
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> definition) {
        StateBuilder builder = StateBuilder.create();
        this.registerState(builder);
        builder.createDefinition(definition);
        this.pendingStateBuilder = builder;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public Holder.Reference<Block> builtInRegistryHolder() {
        return super.builtInRegistryHolder();
    }
    
    @Override
    protected final void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        final BlockState newState = level.getBlockState(pos);
        this.onRemoved(level, pos, state, newState, movedByPiston);
    }
    
    @Override
    protected final void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!state.is(oldState.getBlock())) {
            this.onPlaced((ServerLevel) level, pos, state, oldState, movedByPiston);
        } else {
            this.onChanged((ServerLevel) level, pos, state, oldState, movedByPiston);
        }
    }
    
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.useWithItem(stack, player, level, state, pos, hit);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return this.useWithoutItem(player, level, state, pos, hit);
    }
    
    ///
    /// Used to register BlockProperties to this block.
    ///
    /// @param builder State builder to register properties to
    ///
    protected void registerState(StateBuilder builder) { }
    
    ///
    /// Called when the block is actually removed from the world
    ///
    /// @param level         Level this block is in
    /// @param pos           Position of this block
    /// @param state         Block state before removal. {@link BlockState#getBlock()} is the same as this block.
    /// @param newState      Block state after removal {@link BlockState#getBlock()} is different as this block.
    /// @param movedByPiston Whether this block was moved by a piston
    ///
    protected void onRemoved(ServerLevel level, BlockPos pos, BlockState state, BlockState newState, boolean movedByPiston) { }
    
    ///
    /// Called when the block state changes.
    ///
    /// @param level         Level this block is in
    /// @param pos           Position of this block
    /// @param state         Block state after change. {@link BlockState#getBlock()} is the same as this block.
    /// @param oldState      Block state before change. {@link BlockState#getBlock()} is the same as this block.
    /// @param movedByPiston Whether this block was moved by a piston
    ///
    protected void onChanged(ServerLevel level, BlockPos pos, BlockState state, BlockState oldState, boolean movedByPiston) { }
    
    ///
    /// Called when this block is actually placed into the world.
    ///
    /// @param level         Level this block is in
    /// @param pos           Position of this block
    /// @param state         Block state after placement. {@link BlockState#getBlock()} is the same as this block.
    /// @param oldState      Block state before placement. {@link BlockState#getBlock()} is different as this block.
    /// @param movedByPiston Whether this block was moved by a piston
    ///
    protected void onPlaced(ServerLevel level, BlockPos pos, BlockState state, BlockState oldState, boolean movedByPiston) { }
    
    ///
    /// Called when the user right-clicks on this block with an item.
    ///
    /// @param stack  Item stack that was used to right-click on this block
    /// @param player Player that right-clicked on this block
    /// @param level  Level this block is in
    /// @param state  Block state of this block
    /// @param pos    Position of this block
    /// @param hit    Block hit result
    ///
    /// @return Interaction result
    ///
    protected InteractionResult useWithItem(ItemStack stack, Player player, Level level, BlockState state, BlockPos pos, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
    
    ///
    /// Called when the user right-clicks on this block without an item.
    ///
    /// @param player Player that right-clicked on this block
    /// @param level  Level this block is in
    /// @param state  Block state of this block
    /// @param pos    Position of this block
    /// @param hit    Block hit result
    ///
    /// @return Interaction result
    ///
    protected InteractionResult useWithoutItem(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
    
    ///
    /// Called for the block item of this block
    ///
    /// @param stack   ItemStack with this BlockItem
    /// @param context TooltipContext under which this tooltip is displayed
    /// @param display Hidden tooltips and if they should be displayed or not
    /// @param builder The builder for adding components
    /// @param flag    The ToolTip flags set during this call
    ///
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) { }
    
    ///
    /// Mutable helper for declaring block state properties and their defaults.
    ///
    public static class StateBuilder {
        
        private final Map<Property<?>, Comparable<?>> properties = new HashMap<>();
        
        private StateBuilder() { }
        
        ///
        /// Creates a new empty state builder.
        ///
        /// @return new state builder
        ///
        public static StateBuilder create() {
            return new StateBuilder();
        }
        
        @SuppressWarnings("unchecked")
        private static <T extends Comparable<T>> BlockState setUnchecked(BlockState state, Property<?> property, @Nullable Comparable<?> value) {
            if (value == null) return state; // No default value, so we don't set anything
            return state.setValue((Property<T>) property, (T) value);
        }
        
        ///
        /// Add a BlockProperty to this block.
        ///
        /// @param property     Property to add
        /// @param defaultValue Default value for this property
        /// @param <T>          Property type
        /// @param <V>          Default value type
        ///
        /// @return This builder
        ///
        public <T extends Comparable<T>, V extends T> StateBuilder addValue(Property<@NonNull T> property, V defaultValue) {
            this.properties.put(property, defaultValue);
            return this;
        }
        
        ///
        /// Add a BlockProperty to this block with no default value.
        ///
        /// @param property Property to add
        /// @param <T>      Property type
        ///
        /// @return This builder
        ///
        public <T extends Comparable<T>> StateBuilder addValue(Property<T> property) {
            this.properties.put(property, null);
            return this;
        }
        
        private void createDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            for (Property<?> property : this.properties.keySet()) builder.add(property);
        }
        
        private BlockState applyDefaults(BlockState state) {
            for (Map.Entry<Property<?>, Comparable<?>> entry : this.properties.entrySet()) {
                state = StateBuilder.setUnchecked(state, entry.getKey(), entry.getValue());
            }
            return state;
        }
    }
}
