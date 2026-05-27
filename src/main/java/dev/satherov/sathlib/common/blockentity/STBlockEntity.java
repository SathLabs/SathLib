package dev.satherov.sathlib.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

///
/// Abstract base class for all block entities.
/// 
/// @param <T> The type of the block entity that extends this class.
/// 
public abstract class STBlockEntity<T extends STBlockEntity<T>> extends BlockEntity {
    
    ///
    /// Default constructor enforcing blockentity-type.
    /// 
    /// @param type type of this block entity.
    /// @param pos position of this block entity.
    /// @param state state of the block this block entity is attached to.
    /// 
    public STBlockEntity(BlockEntityType<T> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    ///
    /// Returns this object cast to the given type
    /// 
    /// @return self
    /// 
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}
