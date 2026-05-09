package dev.satherov.sathlib.client.model.conditional;

import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

public interface ConditionalPredicate {
    
    ///
    /// Returns whether this predicate matches the supplied block state and model data.
    ///
    /// @param getter rendering level
    /// @param pos    position being evaluated
    /// @param state  block state being evaluated
    /// @param data   model data at the same position
    ///
    /// @return `true` when the predicate matches
    ///
    boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data);
    
    ///
    /// Identifier of the condition rule.
    ///
    /// @return rule identifier
    ///
    Identifier type();
    
    ///
    /// Codec for the condition rule.
    ///
    /// @return codec for the condition rule
    ///
    MapCodec<? extends ConditionalPredicate> codec();
}
