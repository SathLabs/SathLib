package dev.satherov.sathlib.common.properties;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

///
/// Extracts a value from a {@link PropertyHolder}
///
/// @param <T> the value type
///
/// @see PropertyApplicator
/// @see BlockItemProperty
/// @see PropertyBlockHolder
/// @see PropertyItemHolder
///
@FunctionalInterface
public interface PropertyExtractor<T> {
    
    ///
    /// Extracts a value from a  {@link BlockState} via a {@link Property} or returns a default value if the property is not present.
    ///
    /// @param property     The property to extract
    /// @param defaultValue The default value to return if the property is not present
    /// @param <T>          the type of the property
    ///
    /// @return the extracted or the default value
    ///
    static <T extends Comparable<T>> PropertyExtractor<T> block(Property<T> property, T defaultValue) {
        return (block, _) -> block.state().getValueOrElse(property, defaultValue);
    }
    
    ///
    /// Extracts a value from a {@link BlockEntity} via a getter or returns a default value if the getter returns null.
    ///
    /// @param type         The type of the block entity
    /// @param getter       The getter to extract the value from
    /// @param defaultValue The default value to return if the getter returns null
    /// @param <T>          the type of the value
    /// @param <BE>         the type of the block entity
    ///
    /// @return the extracted or the default value
    ///
    static <T, BE extends BlockEntity> PropertyExtractor<T> blockEntity(Class<BE> type, Function<BE, T> getter, T defaultValue) {
        return (block, _) -> Objects.requireNonNullElse(block.supplyIfPresent(type, getter), defaultValue);
    }
    
    ///
    /// Extracts a value from a {@link ItemStack} via a {@link DataComponentType} or returns a default value if the component is not present.
    ///
    /// @param component    The component to extract
    /// @param defaultValue The default value to return if the component is not present
    /// @param <T>          the type of the component
    ///
    /// @return the extracted or the default value
    ///
    static <T> PropertyExtractor<T> item(DataComponentType<T> component, T defaultValue) {
        return (_, item) -> item.stack().getOrDefault(component, defaultValue);
    }
    
    ///
    /// Extracts a value from a {@link ItemStack} via a {@link Supplier} for the {@link DataComponentType} or returns a default value if the component is not present.
    ///
    /// @param component    The component to extract
    /// @param defaultValue The default value to return if the component is not present
    /// @param <T>          the type of the component
    ///
    /// @return the extracted or the default value
    ///
    static <T> PropertyExtractor<T> item(Supplier<DataComponentType<T>> component, T defaultValue) {
        return (_, item) -> item.stack().getOrDefault(component, defaultValue);
    }
    
    ///
    /// Extracts a value from a {@link PropertyHolder} and returns it.
    ///
    /// @param block The block property holder
    /// @param item  The item property holder
    ///
    /// @return the extracted value
    ///
    T extract(PropertyBlockHolder block, PropertyItemHolder item);
}
