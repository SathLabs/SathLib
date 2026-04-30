package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

///
/// Holds the mask for each side of a block with connected textures.
///
/// @param down  The mask for the block's downward face. {@link ConnectionFace#Y_NEG}
/// @param up    The mask for the block's upward face. {@link ConnectionFace#Y_POS}
/// @param north The mask for the block's northward face. {@link ConnectionFace#Z_NEG}
/// @param south The mask for the block's southward face. {@link ConnectionFace#Z_POS}
/// @param west  The mask for the block's westward face. {@link ConnectionFace#X_NEG}
/// @param east  The mask for the block's eastward face. {@link ConnectionFace#X_POS}
///
public record ConnectedTextureFaceMasks(int down, int up, int north, int south, int west, int east) {
    
    ///
    /// Empty face-mask set with no connected neighbors.
    ///
    public static final ConnectedTextureFaceMasks NONE = new ConnectedTextureFaceMasks(0, 0, 0, 0, 0, 0);
    
    ///
    /// Creates the masks based on the supplied world information and predicate to test.
    ///
    /// @param level     The world to check in.
    /// @param pos       The position of the block in the world.
    /// @param state     The block state of the block in the world.
    /// @param predicate The predicate to test.
    ///
    /// @return The masks for the block or {@link ConnectedTextureFaceMasks#NONE} if the predicate is null.
    ///
    public static ConnectedTextureFaceMasks resolve(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            @Nullable final ConnectedTexturePredicate predicate
    ) {
        if (predicate == null) return ConnectedTextureFaceMasks.NONE;
        return new ConnectedTextureFaceMasks(
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.DOWN, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.UP, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.NORTH, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.SOUTH, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.WEST, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.EAST, predicate)
        );
    }
    
    ///
    /// Resolves the mask for the given face
    ///
    /// @param face The face to resolve the mask for.
    ///
    /// @return The mask for the face.
    ///
    public int mask(final Direction face) {
        return switch (face) {
            case DOWN -> this.down;
            case UP -> this.up;
            case NORTH -> this.north;
            case SOUTH -> this.south;
            case WEST -> this.west;
            case EAST -> this.east;
        };
    }
    
    ///
    /// Resolves the mask for the given face based on the given world information and predicate.
    ///
    /// @param level         The world to check in.
    /// @param pos           The position of the block in the world.
    /// @param state         The block state of the block in the world.
    /// @param faceDirection The direction of the face to resolve the mask for.
    /// @param predicate     The predicate to test.
    ///
    ///
    private static int resolve(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final Direction faceDirection,
            final ConnectedTexturePredicate predicate
    ) {
        final ConnectionFace face = ConnectionFace.of(faceDirection);
        
        final boolean north = ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, 0, 1, predicate);
        final boolean east = ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, 1, 0, predicate);
        final boolean south = ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, 0, -1, predicate);
        final boolean west = ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, -1, 0, predicate);
        
        final boolean northEast = north && east && ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, 1, 1, predicate);
        final boolean southEast = south && east && ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, 1, -1, predicate);
        final boolean southWest = south && west && ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, -1, -1, predicate);
        final boolean northWest = north && west && ConnectedTextureFaceMasks.connects(level, pos, state, faceDirection, face, -1, 1, predicate);
        
        return SpriteMasks.of(north, northEast, east, southEast, south, southWest, west, northWest);
    }
    
    ///
    /// Checks if the given face is allowed to connect to a neighbor block based on the given world information and predicate.
    ///
    /// @param level         The world to check in.
    /// @param pos           The position of the block in the world.
    /// @param state         The block state of the block in the world.
    /// @param faceDirection The direction of the face to check.
    /// @param face          The face to check.
    /// @param eastOffset    The east offset of the neighbor block.
    /// @param northOffset   The north offset of the neighbor block.
    /// @param predicate     The predicate to test.
    ///
    /// @return `true` if the face is allowed to connect to a neighbor block, `false` otherwise.
    ///
    private static boolean connects(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final Direction faceDirection,
            final ConnectionFace face,
            final int eastOffset,
            final int northOffset,
            final ConnectedTexturePredicate predicate
    ) {
        final BlockPos neighborPos = face.offset(pos, eastOffset, northOffset);
        final BlockState neighborState = level.getBlockState(neighborPos);
        final BlockState originAppearance = state.getAppearance(level, pos, faceDirection, neighborState, neighborPos);
        final BlockState neighborAppearance = neighborState.getAppearance(level, neighborPos, faceDirection, state, pos);
        if (originAppearance.isAir() || neighborAppearance.isAir()) return false;
        
        return predicate.connects(new ConnectedTextureContext(
                level,
                pos,
                originAppearance,
                neighborPos,
                neighborAppearance,
                faceDirection
        ));
    }
}
