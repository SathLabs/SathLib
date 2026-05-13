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
/// @param <T> block entity type managed by the block
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
    
    @Override
    default <S extends BlockEntity> @Nullable GameEventListener getListener(ServerLevel level, S blockEntity) {
        return EntityBlock.super.getListener(level, blockEntity);
    }
}
