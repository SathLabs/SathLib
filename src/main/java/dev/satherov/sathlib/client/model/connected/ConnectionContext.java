package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

///
/// The context used for testing connections
///
/// @param level         the block level
/// @param face          the face of the block
/// @param originPos     the position of the block
/// @param originState   the block state of the block
/// @param originData    the model data of the block
/// @param neighborPos   the position of the neighbor block
/// @param neighborState the block state of the neighbor block
/// @param neighborData  the model data of the neighbor block
///
@NothingNull
public record ConnectionContext(
        BlockAndTintGetter level,
        Direction face,
        BlockPos originPos,
        BlockState originState,
        ModelData originData,
        BlockPos neighborPos,
        BlockState neighborState,
        ModelData neighborData
) { }
