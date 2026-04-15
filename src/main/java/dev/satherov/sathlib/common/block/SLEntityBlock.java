package dev.satherov.sathlib.common.block;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;

import org.jspecify.annotations.Nullable;

///
/// The SLEntityBlock interface must be implemented on all blocks which have a block entity associated to them
///
@NothingNull
public interface SLEntityBlock<T extends BlockEntity> extends EntityBlock {
    
    ///
    /// Creates a new block entity at the given position with the given state
    ///
    /// @param pos   position the block entity should be created at
    /// @param state the block state of the block entity
    ///
    /// @return The created block entity
    ///
    @Nullable T newBlockEntity(BlockPos pos, BlockState state);
    
    ///
    /// Called on both sides, before {@link #clientTick(ClientLevel, BlockPos, BlockState, BlockEntity)} and {@link #serverTick(ServerLevel, BlockPos, BlockState, BlockEntity)}
    ///
    /// @param level  the level this block exists in
    /// @param pos    the position of this block
    /// @param state  the current state of this block at the given position
    /// @param entity the block entity at the given position
    ///
    default void commonTick(Level level, BlockPos pos, BlockState state, T entity) { }
    
    ///
    /// Called only on the client side
    ///
    /// @param level  the level this block exists in
    /// @param pos    the position of this block
    /// @param state  the current state of this block at the given position
    /// @param entity the block entity at the given position
    ///
    default void clientTick(ClientLevel level, BlockPos pos, BlockState state, T entity) { }
    
    ///
    /// Called only on the server side
    ///
    /// @param level  the level this block exists in
    /// @param pos    the position of this block
    /// @param state  the current state of this block at the given position
    /// @param entity the block entity at the given position
    ///
    default void serverTick(ServerLevel level, BlockPos pos, BlockState state, T entity) { }
    
    @Override
    @SuppressWarnings("unchecked")
    default <S extends BlockEntity> @Nullable BlockEntityTicker<S> getTicker(Level world, BlockState blockState, BlockEntityType<S> type) {
        return (level, pos, state, blockEntity) -> {
            T entity = (T) blockEntity;
            this.commonTick(level, pos, state, entity);
            if (level instanceof ClientLevel clientLevel) {
                this.clientTick(clientLevel, pos, state, entity);
            } else if (level instanceof ServerLevel serverLevel) {
                this.serverTick(serverLevel, pos, state, entity);
            }
        };
    }
    
    @Override
    default <S extends BlockEntity> @Nullable GameEventListener getListener(ServerLevel level, S blockEntity) {
        return EntityBlock.super.getListener(level, blockEntity);
    }
}
