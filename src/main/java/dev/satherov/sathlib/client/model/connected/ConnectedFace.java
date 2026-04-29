package dev.satherov.sathlib.client.model.connected;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@Getter
@RequiredArgsConstructor
public enum ConnectedFace {
    
    // X-
    X_NEG(Direction.WEST, Direction.UP, Direction.SOUTH),
    
    // X+
    X_POS(Direction.EAST, Direction.UP, Direction.NORTH),
    
    // Y-
    Y_NEG(Direction.DOWN, Direction.SOUTH, Direction.EAST),
    
    // Y+
    Y_POS(Direction.UP, Direction.NORTH, Direction.EAST),
    
    // Z-
    Z_NEG(Direction.NORTH, Direction.UP, Direction.WEST),
    
    // Z+
    Z_POS(Direction.SOUTH, Direction.UP, Direction.EAST);
    
    private final Direction face;
    private final Direction north;
    private final Direction east;
    
    public Direction south() {
        return this.north.getOpposite();
    }
    
    public Direction west() {
        return this.east.getOpposite();
    }
    
    public BlockPos offset(final BlockPos pos, final int eastOffset, final int northOffset) {
        BlockPos result = pos;
        
        if (northOffset > 0) {
            result = result.relative(this.north, northOffset);
        } else if (northOffset < 0) {
            result = result.relative(this.south(), -northOffset);
        }
        
        if (eastOffset > 0) {
            result = result.relative(this.east, eastOffset);
        } else if (eastOffset < 0) {
            result = result.relative(this.west(), -eastOffset);
        }
        
        return result;
    }
    
    public static ConnectedFace of(final Direction face) {
        return switch (face) {
            case DOWN -> ConnectedFace.Y_NEG;
            case UP -> ConnectedFace.Y_POS;
            case NORTH -> ConnectedFace.Z_NEG;
            case SOUTH -> ConnectedFace.Z_POS;
            case WEST -> ConnectedFace.X_NEG;
            case EAST -> ConnectedFace.X_POS;
        };
    }
}
