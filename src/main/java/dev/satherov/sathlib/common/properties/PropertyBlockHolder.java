package dev.satherov.sathlib.common.properties;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

///
/// Holder for a block state and its associated block entity.
///
/// @param state       the block state
/// @param blockEntity the block entity, if any
///
/// @see PropertyItemHolder
///
public record PropertyBlockHolder(BlockState state, @Nullable BlockEntity blockEntity) implements PropertyHolder {
    
    ///
    /// Performs an action if the block entity is of the specified type.
    ///
    /// @param type     the block entity type
    /// @param consumer the action to perform
    /// @param <BE>     the block entity type
    ///
    public <BE extends BlockEntity> void doIfPresent(Class<BE> type, Consumer<BE> consumer) {
        if (type.isInstance(this.blockEntity)) consumer.accept(type.cast(this.blockEntity));
    }
    
    ///
    /// Supplies a value if the block entity is of the specified type.
    ///
    /// @param type     the block entity type
    /// @param consumer the function to apply to the block entity
    /// @param <T>      the return type
    /// @param <BE>     the block entity type
    ///
    /// @return supplied value, or `null` when the block entity does not match
    ///
    public <T, BE extends BlockEntity> @Nullable T supplyIfPresent(Class<BE> type, Function<BE, T> consumer) {
        if (type.isInstance(this.blockEntity)) return consumer.apply(type.cast(this.blockEntity));
        return null;
    }
}
