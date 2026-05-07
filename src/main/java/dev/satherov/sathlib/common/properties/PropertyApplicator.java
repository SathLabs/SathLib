package dev.satherov.sathlib.common.properties;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

///
/// Allows setting a value on a {@link PropertyHolder} instance.
///
/// @param <T> the value type
/// @param <H> the property holder type
///
/// @see PropertyExtractor
/// @see BlockItemProperty
/// @see PropertyBlockHolder
/// @see PropertyItemHolder
///
@FunctionalInterface
public interface PropertyApplicator<T, H extends PropertyHolder> {
    
    ///
    /// Sets a value and then returns the modified property holder
    ///
    /// @param block the block property holder
    /// @param item  the item property holder
    /// @param value the value to set
    ///
    /// @return the modified property holder
    ///
    H apply(PropertyBlockHolder block, PropertyItemHolder item, T value);
    
    ///
    /// Creates a property applicator for setting a block state property.
    ///
    /// @param property the block state property to modify
    /// @param <T>      the value type
    ///
    /// @return the property applicator
    ///
    static <T extends Comparable<T>> PropertyApplicator<T, PropertyBlockHolder> block(Property<T> property) {
        return (block, _, value) -> {
            final BlockState updated = block.state().setValue(property, value);
            return new PropertyBlockHolder(updated, block.blockEntity());
        };
    }
    
    ///
    /// Creates a property applicator for setting a value on a block entity.
    ///
    /// @param type   the type of the block entity
    /// @param setter the setter method to call
    /// @param <T>    the value type
    /// @param <BE>   the block entity type
    ///
    /// @return the property applicator
    ///
    static <T, BE extends BlockEntity> PropertyApplicator<T, PropertyBlockHolder> blockEntity(Class<BE> type, BiConsumer<BE, T> setter) {
        return (block, _, value) -> {
            block.doIfPresent(type, entity -> setter.accept(entity, value));
            return block;
        };
    }
    
    ///
    /// Creates a property applicator for setting a data component.
    ///
    /// @param component the data component to modify
    /// @param <T>       the data component type
    ///
    /// @return the property applicator
    ///
    static <T> PropertyApplicator<T, PropertyItemHolder> item(DataComponentType<T> component) {
        return (_, item, value) -> {
            item.stack().set(component, value);
            return item;
        };
    }
    
    ///
    /// Creates a property applicator for setting a data component.
    ///
    /// @param component the supplier for the data component to modify
    /// @param <T>       the data component type
    ///
    /// @return the property applicator
    ///
    static <T> PropertyApplicator<T, PropertyItemHolder> item(Supplier<DataComponentType<T>> component) {
        return (_, item, value) -> {
            item.stack().set(component.get(), value);
            return item;
        };
    }
}
