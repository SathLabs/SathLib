package dev.satherov.sathlib.client.model.connected;

import net.neoforged.neoforge.common.extensions.IBlockStateExtension;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;

///
/// Data record holding the information
///
/// @param level              the block level
/// @param originPos          the position of the block
/// @param originState        the state of the block. This is the {@link IBlockStateExtension#getAppearance(BlockAndLightGetter, BlockPos, Direction, BlockState, BlockPos)} state
/// @param neighborPos        the position of the neighbor block
/// @param neighborState      the state of the neighbor block. This is the {@link IBlockStateExtension#getAppearance(BlockAndLightGetter, BlockPos, Direction, BlockState, BlockPos)} state
/// @param face               the face of the block
///
public record ConnectedTextureContext(
        BlockAndTintGetter level,
        BlockPos originPos,
        BlockState originState,
        BlockPos neighborPos,
        BlockState neighborState,
        Direction face
) { }
