package dev.satherov.sathlib.client.render.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/// Directions around a single rendered face, ordered to match standard 47-tile
/// blob masks.
public enum SLConnectedTextureDirection {
    /// Neighbor to the local right of the rendered face.
    RIGHT(SLConnectedTextureMasks.RIGHT),
    /// Neighbor to the local top-right of the rendered face.
    TOP_RIGHT(SLConnectedTextureMasks.TOP_RIGHT),
    /// Neighbor directly above the rendered face.
    TOP(SLConnectedTextureMasks.TOP),
    /// Neighbor to the local top-left of the rendered face.
    TOP_LEFT(SLConnectedTextureMasks.TOP_LEFT),
    /// Neighbor to the local left of the rendered face.
    LEFT(SLConnectedTextureMasks.LEFT),
    /// Neighbor to the local bottom-left of the rendered face.
    BOTTOM_LEFT(SLConnectedTextureMasks.BOTTOM_LEFT),
    /// Neighbor directly below the rendered face.
    BOTTOM(SLConnectedTextureMasks.BOTTOM),
    /// Neighbor to the local bottom-right of the rendered face.
    BOTTOM_RIGHT(SLConnectedTextureMasks.BOTTOM_RIGHT);
    
    private final @SLConnectedTextureMask int bit;
    
    SLConnectedTextureDirection(@SLConnectedTextureMask int bit) {
        this.bit = bit;
    }
    
    /// Returns the bit flag represented by this local direction.
    ///
    /// @return connected-texture mask bit
    public @SLConnectedTextureMask int bit() {
        return this.bit;
    }
    
    /// Converts this face-local direction to a world-space block offset.
    ///
    /// @param face face being rendered
    ///
    /// @return neighbor offset for the direction
    public BlockPos offset(Direction face) {
        Basis basis = Basis.of(face);
        return switch (this) {
            case RIGHT -> BlockPos.ZERO.relative(basis.right());
            case TOP_RIGHT -> BlockPos.ZERO.relative(basis.top()).relative(basis.right());
            case TOP -> BlockPos.ZERO.relative(basis.top());
            case TOP_LEFT -> BlockPos.ZERO.relative(basis.top()).relative(basis.left());
            case LEFT -> BlockPos.ZERO.relative(basis.left());
            case BOTTOM_LEFT -> BlockPos.ZERO.relative(basis.bottom()).relative(basis.left());
            case BOTTOM -> BlockPos.ZERO.relative(basis.bottom());
            case BOTTOM_RIGHT -> BlockPos.ZERO.relative(basis.bottom()).relative(basis.right());
        };
    }
    
    private record Basis(Direction top, Direction right) {
        
        private Direction bottom() {
            return this.top.getOpposite();
        }
        
        private Direction left() {
            return this.right.getOpposite();
        }
        
        private static Basis of(Direction face) {
            return switch (face) {
                case DOWN -> new Basis(Direction.SOUTH, Direction.EAST);
                case UP -> new Basis(Direction.NORTH, Direction.EAST);
                case NORTH -> new Basis(Direction.UP, Direction.WEST);
                case SOUTH -> new Basis(Direction.UP, Direction.EAST);
                case WEST -> new Basis(Direction.UP, Direction.NORTH);
                case EAST -> new Basis(Direction.UP, Direction.SOUTH);
            };
        }
    }
}
