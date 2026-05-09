package dev.satherov.sathlib.client.model.connected;

import net.neoforged.neoforge.model.data.ModelData;

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
public record ConnectionFaceMasks(int down, int up, int north, int south, int west, int east) {
    
    ///
    /// Positive offset.
    ///
    public static final int OFFSET_POS = 1;
    
    ///
    /// Zero offset.
    ///
    public static final int OFFSET_ZERO = 0;
    
    ///
    /// Negative offset.
    ///
    public static final int OFFSET_NEG = -1;
    
    ///
    /// Default mask for a block with no connections
    ///
    public static final ConnectionFaceMasks NONE = new ConnectionFaceMasks(SpriteMasks.NONE, SpriteMasks.NONE, SpriteMasks.NONE, SpriteMasks.NONE, SpriteMasks.NONE, SpriteMasks.NONE);
    
    ///
    /// Creates the masks based on the supplied world information and predicate to test.
    ///
    /// @param getter The world to check in.
    /// @param pos    The position of the block in the world.
    /// @param state  The block state of the block in the world.
    /// @param rule   The rule to test.
    ///
    /// @return The masks for the block or {@link ConnectionFaceMasks#NONE} if the rule is null.
    ///
    public static ConnectionFaceMasks resolve(
            final BlockAndTintGetter getter,
            final BlockPos pos,
            final BlockState state,
            final @Nullable ConnectionPredicate rule
    ) {
        if (rule == null) return ConnectionFaceMasks.NONE;
        return new ConnectionFaceMasks(
                ConnectionFaceMasks.resolveSpriteMask(getter, pos, state, Direction.DOWN, rule),
                ConnectionFaceMasks.resolveSpriteMask(getter, pos, state, Direction.UP, rule),
                ConnectionFaceMasks.resolveSpriteMask(getter, pos, state, Direction.NORTH, rule),
                ConnectionFaceMasks.resolveSpriteMask(getter, pos, state, Direction.SOUTH, rule),
                ConnectionFaceMasks.resolveSpriteMask(getter, pos, state, Direction.WEST, rule),
                ConnectionFaceMasks.resolveSpriteMask(getter, pos, state, Direction.EAST, rule)
        );
    }
    
    ///
    /// Resolves the mask for the given face based on the given world information and rule.
    ///
    /// @param level         The world to check in.
    /// @param pos           The position of the block in the world.
    /// @param state         The block state of the block in the world.
    /// @param faceDirection The direction of the face to resolve the mask for.
    /// @param rule          The rule to test.
    ///
    ///
    private static int resolveSpriteMask(
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final Direction faceDirection,
            final ConnectionPredicate rule
    ) {
        final ConnectionFace face = ConnectionFace.of(faceDirection);
        
        final boolean north = ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_POS, ConnectionFaceMasks.OFFSET_ZERO, rule);
        final boolean east = ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_ZERO, ConnectionFaceMasks.OFFSET_POS, rule);
        final boolean south = ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_NEG, ConnectionFaceMasks.OFFSET_ZERO, rule);
        final boolean west = ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_ZERO, ConnectionFaceMasks.OFFSET_NEG, rule);
        
        final boolean northEast = north && east && ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_POS, ConnectionFaceMasks.OFFSET_POS, rule);
        final boolean northWest = north && west && ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_POS, ConnectionFaceMasks.OFFSET_ZERO, rule);
        final boolean southEast = south && east && ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_NEG, ConnectionFaceMasks.OFFSET_POS, rule);
        final boolean southWest = south && west && ConnectionFaceMasks.shouldConnect(level, pos, state, faceDirection, face, ConnectionFaceMasks.OFFSET_NEG, ConnectionFaceMasks.OFFSET_NEG, rule);
        
        return SpriteMasks.of(north, northEast, east, southEast, south, southWest, west, northWest);
    }
    
    ///
    /// Checks if the given face is allowed to connect to a neighbor block based on the given world information and rule.
    ///
    /// @param getter        The world to check in.
    /// @param pos           The position of the block in the world.
    /// @param state         The block state of the block in the world.
    /// @param faceDirection The direction of the face to check.
    /// @param face          The face to check.
    /// @param eastOffset    The east offset of the neighbor block.
    /// @param northOffset   The north offset of the neighbor block.
    /// @param rule          The rule to test.
    ///
    /// @return `true` if the face is allowed to connect to a neighbor block, `false` otherwise.
    ///
    private static boolean shouldConnect(
            final BlockAndTintGetter getter,
            final BlockPos pos,
            final BlockState state,
            final Direction faceDirection,
            final ConnectionFace face,
            final int northOffset,
            final int eastOffset,
            final ConnectionPredicate rule
    ) {
        final BlockPos neighborPos = face.offset(pos, eastOffset, northOffset);
        final BlockState neighborState = getter.getBlockState(neighborPos);
        if (state.isAir() || neighborState.isAir()) return false;
        
        final BlockState originAppearance = state.getAppearance(getter, pos, faceDirection, neighborState, neighborPos);
        final BlockState neighborAppearance = neighborState.getAppearance(getter, neighborPos, faceDirection.getOpposite(), state, pos);
        if (originAppearance.isAir() || neighborAppearance.isAir()) return false;
        
        final ModelData originModelData = getter.getModelData(pos);
        final ModelData neighborModelData = getter.getModelData(neighborPos);
        
        return rule.shouldConnect(new ConnectionContext(
                getter,
                faceDirection,
                pos,
                originAppearance,
                originModelData,
                neighborPos,
                neighborAppearance,
                neighborModelData
        ));
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
}
