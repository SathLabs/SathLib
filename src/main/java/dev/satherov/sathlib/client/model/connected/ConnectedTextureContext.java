package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

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
