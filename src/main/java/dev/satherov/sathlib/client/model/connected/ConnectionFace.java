package dev.satherov.sathlib.client.model.connected;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

///
/// Enum representing the 6 faces of a block and the direction this face considers `up` and `right`
///
@Getter
@RequiredArgsConstructor
@SuppressWarnings("doclint:missing")
public enum ConnectionFace {
    ///
    ///  negative X Direction, West in world-space
    ///
    X_NEG(Direction.WEST, Direction.UP, Direction.SOUTH),
    ///
    ///  positive X Direction, East in world-space
    ///
    X_POS(Direction.EAST, Direction.UP, Direction.NORTH),
    /// negative Y Direction, Down in world-space
    ///
    Y_NEG(Direction.DOWN, Direction.SOUTH, Direction.EAST),
    ///
    ///  positive Y Direction, Up in world-space
    ///
    Y_POS(Direction.UP, Direction.NORTH, Direction.EAST),
    ///
    ///  negative Z Direction, North in world-space
    ///
    Z_NEG(Direction.NORTH, Direction.UP, Direction.WEST),
    ///
    ///  positive Z Direction, South in world-space
    ///
    Z_POS(Direction.SOUTH, Direction.UP, Direction.EAST);
    
    private final Direction face;
    private final Direction north;
    private final Direction east;
    
    ///
    /// Resolves the connected face based on the world-space direction.
    ///
    /// @param face the world-space direction
    ///
    /// @return the face
    ///
    public static ConnectionFace of(final Direction face) {
        return switch (face) {
            case DOWN -> ConnectionFace.Y_NEG;
            case UP -> ConnectionFace.Y_POS;
            case NORTH -> ConnectionFace.Z_NEG;
            case SOUTH -> ConnectionFace.Z_POS;
            case WEST -> ConnectionFace.X_NEG;
            case EAST -> ConnectionFace.X_POS;
        };
    }
    
    ///
    /// Returns the world-space direction opposite this face's local north direction.
    ///
    /// @return local south direction in world space
    ///
    public Direction getSouth() {
        return this.north.getOpposite();
    }
    
    ///
    /// Returns the world-space direction opposite this face's local east direction.
    ///
    /// @return local west direction in world space
    ///
    public Direction getWest() {
        return this.east.getOpposite();
    }
    
    ///
    /// Calculates the offest block position based on the face orientation and the given offsets.
    ///
    /// @param origin      the base block position
    /// @param eastOffset  east offset
    /// @param northOffset north offset
    ///
    /// @return the offset block position
    ///
    public BlockPos offset(final BlockPos origin, final int eastOffset, final int northOffset) {
        BlockPos result = origin;
        
        if (northOffset > 0) {
            result = result.relative(this.north, northOffset);
        } else if (northOffset < 0) {
            result = result.relative(this.getSouth(), -northOffset);
        }
        
        if (eastOffset > 0) {
            result = result.relative(this.east, eastOffset);
        } else if (eastOffset < 0) {
            result = result.relative(this.getWest(), -eastOffset);
        }
        
        return result;
    }
}
