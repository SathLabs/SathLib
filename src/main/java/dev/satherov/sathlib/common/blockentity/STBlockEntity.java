package dev.satherov.sathlib.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class STBlockEntity<T extends STBlockEntity<T>> extends BlockEntity {
    
    public STBlockEntity(BlockEntityType<T> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }
    
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}
