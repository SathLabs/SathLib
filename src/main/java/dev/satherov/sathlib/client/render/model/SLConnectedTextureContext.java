package dev.satherov.sathlib.client.render.model;

import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/// Resolved runtime data for a connected-texture block.
///
/// @param level     level used for block/model-data lookups
/// @param pos       block position being evaluated
/// @param state     blockstate being evaluated
/// @param modelData resolved model data at the position
public record SLConnectedTextureContext(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
}
