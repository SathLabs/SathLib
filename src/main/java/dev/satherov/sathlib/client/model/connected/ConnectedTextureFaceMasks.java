package dev.satherov.sathlib.client.model.connected;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

public record ConnectedTextureFaceMasks(int down, int up, int north, int south, int west, int east) {
    
    public static final ConnectedTextureFaceMasks NONE = new ConnectedTextureFaceMasks(0, 0, 0, 0, 0, 0);
    
    public static ConnectedTextureFaceMasks resolve(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            @Nullable final ConnectedTexturePredicate predicate
    ) {
        if (predicate == null) {
            return ConnectedTextureFaceMasks.NONE;
        }
        
        return new ConnectedTextureFaceMasks(
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.DOWN, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.UP, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.NORTH, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.SOUTH, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.WEST, predicate),
                ConnectedTextureFaceMasks.resolve(level, pos, state, Direction.EAST, predicate)
        );
    }
    
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
    
    private static int resolve(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final Direction faceDirection,
            final ConnectedTexturePredicate predicate
    ) {
        final ConnectedFace face = ConnectedFace.of(faceDirection);
        
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
    
    private static boolean connects(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final Direction faceDirection,
            final ConnectedFace face,
            final int eastOffset,
            final int northOffset,
            final ConnectedTexturePredicate predicate
    ) {
        final BlockPos neighborPos = face.offset(pos, eastOffset, northOffset);
        final BlockState neighborState = level.getBlockState(neighborPos);
        final BlockState originAppearance = state.getAppearance(level, pos, faceDirection, neighborState, neighborPos);
        final BlockState neighborAppearance = neighborState.getAppearance(level, neighborPos, faceDirection, state, pos);
        
        return predicate.connects(new ConnectedTextureContext(
                level,
                pos,
                state,
                originAppearance,
                neighborPos,
                neighborState,
                neighborAppearance,
                faceDirection
        ));
    }
}
