package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

///
/// Data record holding the information
///
/// @param level              the block level
/// @param originPos          the position of the block
/// @param originState        the state of the block
/// @param originAppearance   the appearance of the block
/// @param neighborPos        the position of the neighbor block
/// @param neighborState      the state of the neighbor block
/// @param neighborAppearance the appearance of the neighbor block
/// @param face               the face of the block
///
public record ConnectedTextureContext(
        BlockAndTintGetter level,
        BlockPos originPos,
        BlockState originState,
        BlockState originAppearance,
        BlockPos neighborPos,
        BlockState neighborState,
        BlockState neighborAppearance,
        Direction face
) { }
