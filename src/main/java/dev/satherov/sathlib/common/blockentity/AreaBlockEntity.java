package dev.satherov.sathlib.common.blockentity;

import lombok.Getter;
import lombok.Setter;

import dev.satherov.sathlib.util.AreaTracker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;

public abstract class AreaBlockEntity<T extends AreaBlockEntity<T>> extends STBlockEntity<T> {
    
    private @Getter BoundingBox area = new BoundingBox(BlockPos.ZERO);
    private @Getter @Setter boolean visible = true;
    
    public AreaBlockEntity(BlockEntityType<T> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }
    
    public abstract AreaTracker<T> getTracker();
    
    public void updateArea(@NotNull BoundingBox area) {
        if (this.area.equals(area)) return;
        
        this.area = area;
        
        if (this.getLevel() != null && !this.isRemoved()) this.getTracker().update(this.self(), area);
        
        this.setChanged();
    }
    
    public BoundingBox getWorldArea(@NotNull BlockPos pos) {
        return this.area.moved(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public BoundingBox getWorldArea() {
        return this.getWorldArea(this.getBlockPos());
    }
    
    @Override
    protected void loadAdditional(final @NotNull ValueInput input) {
        super.loadAdditional(input);
        this.area = input.read("area", BoundingBox.CODEC).orElse(this.area);
    }
    
    @Override
    protected void saveAdditional(final @NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.store("area", BoundingBox.CODEC, this.area);
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        this.getTracker().register(this.self());
    }
    
    @Override
    public void setRemoved() {
        this.getTracker().unregister(this.self());
        super.setRemoved();
    }
}
