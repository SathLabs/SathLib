package dev.satherov.sathlib.common.properties;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import dev.satherov.sathlib.client.lang.SLDisplayable;
import dev.satherov.sathlib.client.lang.SLTranslatable;
import dev.satherov.sathlib.core.annotations.NothingNull;
import dev.satherov.sathlib.network.chat.SLComponent;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

///
/// A SLBLockProperty defines a property that can be used to modify a {@link BlockState}, {@link ItemStack} or {@link BlockEntity}
/// from any of the other two defined. An {@link Extractor} extracts a certain value from the origin and then applies it to the target
/// via the defined {@link Updater}
///
/// For example, an item may have the ability to modify the waterlogged state of a block. The Item therefore has a boolean {@link DataComponentType}
/// which mirrors the blocks {@link BlockStateProperties#WATERLOGGED} state. Define an {@link Extractor} which returns the boolean value of the
/// data component from the item and an {@link Updater} that sets the property of the state.
///
/// ```java
/// // You may want to do a validity check inside Updater or Extractor, but you can also do these at the callsite instead
/// private static SLBLockProperty<Boolean> PROPERTY = SLBlockProperty.builder(Identifier.fromNameSpaceAndPath("mymod", "waterlogged"), Boolean.class)
///     .stackExtractor(stack -> SLPropertyValue.of(stack.get(MyDataComponents.WATERLOGGED))) // Returns the boolean value.
///     .stateUpdater((state, val) -> stack.setValue(BlockStateProperties.WATERLOGGED, val)) // updates the waterlogged property to the given value.
///     .build()
///
/// private void method(ItemStack stack, BlockState state) {
///     BlockState updated = PROPERTY.updateFromStack(stack, state); // This will apply the value of the data component from the stack to the state
///     ItemStack updated = PROPERTY.updateFromState(state, stack); // This will do nothing as the stackUpdater and stateExtractor are not set
/// }
/// ```
///
/// @param <T> logical property value type
/// @param <E> block entity type supported by the property
///
/// @see SLPropertyContainer
///
@Slf4j
@Getter
@Builder
@NothingNull
@Accessors(fluent = true)
@SuppressWarnings("doclint:missing")
public class SLProperty<T, E extends BlockEntity> implements SLDisplayable {
    
    protected final Identifier identifier;
    protected final Class<T> typeClass;
    protected final Class<E> entityClass;
    protected final SLTranslatable name;
    
    protected final @Nullable Displayer<T> valueDisplayer;
    protected final @Nullable Displayer<T> tooltipDisplayer;
    
    protected final Cycler<T> cycler;
    
    protected final @Nullable Updater<T, ItemStack> stackUpdater;
    protected final @Nullable StateUpdater<T> stateUpdater;
    protected final @Nullable Updater<T, E> blockEntityUpdater;
    
    protected final SLProperty.@Nullable Extractor<T, ItemStack> stackExtractor;
    protected final SLProperty.@Nullable Extractor<T, BlockState> stateExtractor;
    protected final SLProperty.@Nullable Extractor<T, E> blockEntityExtractor;
    
    ///
    /// Starts a new property builder for properties that use the generic {@link BlockEntity} type.
    ///
    /// @param identifier unique identifier of the property
    /// @param type       logical property value type
    /// @param <T>        property value type
    ///
    /// @return a builder configured with the given identifier and value type
    ///
    public static <T> SLPropertyBuilder<T, BlockEntity> builder(Identifier identifier, Class<T> type) {
        return new SLPropertyBuilder<T, BlockEntity>().identifier(identifier).typeClass(type).entityClass(BlockEntity.class);
    }
    
    ///
    /// Starts a new property builder for properties that target a specific block entity type.
    ///
    /// @param identifier  unique identifier of the property
    /// @param type        logical property value type
    /// @param blockEntity supported block entity class
    /// @param <T>         property value type
    /// @param <E>         supported block entity type
    ///
    /// @return a builder configured with the given identifier, value type, and block entity type
    ///
    public static <T, E extends BlockEntity> SLPropertyBuilder<T, E> builder(Identifier identifier, Class<T> type, Class<E> blockEntity) {
        return new SLPropertyBuilder<T, E>().identifier(identifier).typeClass(type).entityClass(blockEntity);
    }
    
    ///
    /// Cycles the property in the given direction
    ///
    /// @param dir   the direction to cycle in. `true` for forward, `false` for backwards
    /// @param value the original value
    ///
    /// @return the updated value
    ///
    public T cycle(boolean dir, T value) {
        return this.cycler.cycle(dir, value);
    }
    
    ///
    /// Cycles the property on an ItemStack in the given direction. Requires both an updater and a resolver
    ///
    /// @param dir   the direction to cycle in. `true` for forward, `false` for backwards
    /// @param stack the ItemStack to update
    ///
    public void cycle(boolean dir, ItemStack stack) {
        T value = this.extract(stack);
        if (value == null) return;
        T updated = this.cycle(dir, value);
        if (!Objects.equals(value, updated)) this.update(stack, updated);
    }
    
    ///
    /// Cycles the property on an BlockState in the given direction. Requires both an updater and a resolver
    ///
    /// @param dir   the direction to cycle in. `true` for forward, `false` for backwards
    /// @param state the BlockState to update
    ///
    /// @return the updated block state
    ///
    public BlockState cycle(boolean dir, BlockState state) {
        T value = this.extract(state);
        if (value == null) return state;
        T updated = this.cycle(dir, value);
        if (!Objects.equals(value, updated)) return this.update(state, updated);
        return state;
    }
    
    ///
    /// Cycles the property on an BlockEntity in the given direction. Requires both an updater and a resolver
    ///
    /// @param dir    the direction to cycle in. `true` for forward, `false` for backwards
    /// @param entity the BlockEntity to update
    ///
    public void cycle(boolean dir, E entity) {
        T value = this.extract(entity);
        if (value == null) return;
        T updated = this.cycle(dir, value);
        if (!Objects.equals(value, updated)) this.update(entity, updated);
    }
    
    ///
    /// Updates the ItemStack with the given value
    ///
    /// @param stack stack to update
    /// @param value value to update with
    ///
    public void update(ItemStack stack, T value) {
        if (this.stackUpdater == null) return;
        this.stackUpdater.update(stack, value);
    }
    
    ///
    /// Updates the BlockState with the given value
    ///
    /// @param state stack to update
    /// @param value value to update with
    ///
    /// @return updated block state
    ///
    public BlockState update(BlockState state, T value) {
        if (this.stateUpdater == null) return state;
        return this.stateUpdater.update(state, value);
    }
    
    ///
    /// Updates the BlockEntity with the given value
    ///
    /// @param entity stack to update
    /// @param value  value to update with
    ///
    public void update(E entity, T value) {
        if (this.blockEntityUpdater == null) return;
        this.blockEntityUpdater.update(entity, value);
    }
    
    ///
    /// Resolves the current value state from the given stack.
    ///
    /// @param stack stack to inspect
    ///
    /// @return resolved property value or {@link SLPropertyValue#empty()} when unavailable
    ///
    public SLPropertyValue<T> resolve(ItemStack stack) {
        if (this.stackExtractor == null) return SLPropertyValue.empty();
        return this.getPropertyOrEmpty(this.stackExtractor.resolve(stack));
    }
    
    ///
    /// Resolves the current value state from the given block state.
    ///
    /// @param state state to inspect
    ///
    /// @return resolved property value or {@link SLPropertyValue#empty()} when unavailable
    ///
    public SLPropertyValue<T> resolve(BlockState state) {
        if (this.stateExtractor == null) return SLPropertyValue.empty();
        return this.getPropertyOrEmpty(this.stateExtractor.resolve(state));
    }
    
    ///
    /// Resolves the current value state from the given block entity.
    ///
    /// @param entity entity to inspect
    ///
    /// @return resolved property value or {@link SLPropertyValue#empty()} when unavailable
    ///
    public SLPropertyValue<T> resolve(E entity) {
        if (this.blockEntityExtractor == null) return SLPropertyValue.empty();
        return this.getPropertyOrEmpty(this.blockEntityExtractor.resolve(entity));
    }
    
    ///
    /// Converts nullable resolver output into a non-null {@link SLPropertyValue}.
    ///
    /// @param value resolver result that may be `null`
    ///
    /// @return the given value, or {@link SLPropertyValue#empty()} when the input is `null`
    ///
    private SLPropertyValue<T> getPropertyOrEmpty(@Nullable SLPropertyValue<T> value) {
        return value != null ? value : SLPropertyValue.empty();
    }
    
    ///
    /// Gets a value of the given type from the ItemStack
    ///
    /// @param stack stack to get value from
    ///
    /// @return extracted value
    ///
    public @Nullable T extract(ItemStack stack) {
        return this.resolve(stack).value();
    }
    
    ///
    /// Gets a value of the given type from the BlockState
    ///
    /// @param state state to get value from
    ///
    /// @return extracted value
    ///
    public @Nullable T extract(BlockState state) {
        return this.resolve(state).value();
    }
    
    ///
    /// Gets a value of the given type from the BlockEntity
    ///
    /// @param entity entity to get value from
    ///
    /// @return extracted value
    ///
    public @Nullable T extract(E entity) {
        return this.resolve(entity).value();
    }
    
    ///
    /// Updates the given BlockState from the given ItemStack
    ///
    /// @param stack item stack to get value from
    /// @param state block state to update
    ///
    /// @return updated block state
    ///
    public BlockState updateFromStack(ItemStack stack, BlockState state) {
        T value = this.extract(stack);
        if (value == null) return state;
        return this.update(state, value);
    }
    
    ///
    /// Updates the given BlockEntity from the given ItemStack
    ///
    /// @param stack  item stack to get value from
    /// @param entity block entity to update
    ///
    public void updateFromStack(ItemStack stack, E entity) {
        T value = this.extract(stack);
        if (value == null) return;
        this.update(entity, value);
    }
    
    ///
    /// Updates the given ItemStack from the given BlockState
    ///
    /// @param state block state to get value from
    /// @param stack item stack to update
    ///
    public void updateFromState(BlockState state, ItemStack stack) {
        T value = this.extract(state);
        if (value == null) return;
        this.update(stack, value);
    }
    
    ///
    /// Updates the given BlockEntity from the given BlockState
    ///
    /// @param state  block state to get value from
    /// @param entity block entity to update
    ///
    public void updateFromState(BlockState state, E entity) {
        T value = this.extract(state);
        if (value == null) return;
        this.update(entity, value);
    }
    
    ///
    /// Updates the given ItemStack from the given BlockEntity
    ///
    /// @param entity block entity to get value from
    /// @param stack  item stack to update
    ///
    public void updateFromBlockEntity(E entity, ItemStack stack) {
        T value = this.extract(entity);
        if (value == null) return;
        this.update(stack, value);
    }
    
    ///
    /// Updates the given BlockState from the given BlockEntity
    ///
    /// @param entity block entity to get value from
    /// @param state  block state to update
    ///
    /// @return updated block state
    ///
    public BlockState updateFromBlockEntity(E entity, BlockState state) {
        T value = this.extract(entity);
        if (value == null) return state;
        return this.update(state, value);
    }
    
    ///
    /// Builds the display component for this property.
    /// The first argument may be an {@link ItemStack}, {@link BlockState}, or compatible {@link BlockEntity}
    /// used to resolve the current property value.
    ///
    /// @param args optional context objects used to resolve the current property state
    ///
    /// @return display component containing the property name and, when available, its current value
    ///
    @Override
    public SLComponent display(@Nullable Object... args) {
        final SLComponent component = SLComponent.empty().append(this.name.translate(ChatFormatting.GRAY));
        
        for (Object arg : args) {
            SLComponent result = this.resolveComponent(component, arg);
            if (result != null) return result;
        }
        
        return component;
    }
    
    private @Nullable SLComponent resolveComponent(SLComponent component, @Nullable Object arg) {
        final SLPropertyValue<T> value = switch (arg) {
            case ItemStack stack -> this.resolve(stack);
            case BlockState state -> this.resolve(state);
            case BlockEntity entity when this.entityClass.isInstance(entity) -> this.resolve(this.entityClass.cast(entity));
            case null, default -> SLPropertyValue.empty();
        };
        if (value.isEmpty()) return null;
        return this.displayValue(component, value);
    }
    
    ///
    /// Appends the resolved value representation to the given base property component.
    ///
    /// @param component base component containing the property name
    /// @param value     resolved property value state
    ///
    /// @return combined property display component
    ///
    private SLComponent displayValue(SLComponent component, SLPropertyValue<T> value) {
        if (value.isEmpty() && value.display() == null) return component;
        return component.append(Component.literal(": ").withStyle(ChatFormatting.GRAY)).append(this.value(value));
    }
    
    ///
    /// Displays the current value extracted from the given ItemStack
    ///
    /// @param stack ItemStack to read value from
    ///
    /// @return SLComponent with the display of the current value
    ///
    public SLComponent value(ItemStack stack) {
        return this.value(this.resolve(stack));
    }
    
    ///
    /// Displays the current value extracted from the given BlockState
    ///
    /// @param state BlockState to read value from
    ///
    /// @return SLComponent with the display of the current value
    ///
    public SLComponent value(BlockState state) {
        return this.value(this.resolve(state));
    }
    
    ///
    /// Displays the current value extracted from the given BlockEntity
    ///
    /// @param entity BlockEntity to read value from
    ///
    /// @return SLComponent with the display of the current value
    ///
    public SLComponent value(E entity) {
        return this.value(this.resolve(entity));
    }
    
    ///
    /// Formats the given value to its correct display
    ///
    /// @param value value to format
    ///
    /// @return SLComponent with the display of the value
    ///
    public SLComponent value(T value) {
        return this.value(SLPropertyValue.of(value));
    }
    
    ///
    /// Resolves the tooltip component for the value currently stored on the given stack.
    ///
    /// @param stack stack to inspect
    ///
    /// @return resolved tooltip component or `null`
    ///
    public SLComponent tooltip(ItemStack stack) {
        return this.tooltip(this.resolve(stack));
    }
    
    ///
    /// Resolves the tooltip component for the value currently stored on the given block state.
    ///
    /// @param state state to inspect
    ///
    /// @return resolved tooltip component or `null`
    ///
    public SLComponent tooltip(BlockState state) {
        return this.tooltip(this.resolve(state));
    }
    
    ///
    /// Resolves the tooltip component for the value currently stored on the given block entity.
    ///
    /// @param entity entity to inspect
    ///
    /// @return resolved tooltip component or `null`
    ///
    public SLComponent tooltip(E entity) {
        return this.tooltip(this.resolve(entity));
    }
    
    ///
    /// Resolves the tooltip component for the given value.
    ///
    /// @param value value to display
    ///
    /// @return resolved tooltip component or `null`
    ///
    public SLComponent tooltip(T value) {
        return this.tooltip(SLPropertyValue.of(value));
    }
    
    ///
    /// Builds the display component for a resolved property value.
    ///
    /// @param resolved resolved property value state
    ///
    /// @return display component for the resolved value
    ///
    private SLComponent value(SLPropertyValue<T> resolved) {
        if (resolved.display() != null) return SLProperty.copy(resolved.display());
        if (resolved.value() == null) return SLComponent.empty();
        
        SLComponent display = this.defaultValueDisplay(resolved.value());
        if (this.valueDisplayer != null) display = this.valueDisplayer.display(resolved.value());
        return display;
    }
    
    ///
    /// Builds the tooltip component for a resolved property value.
    ///
    /// @param resolved resolved property value state
    ///
    /// @return tooltip component for the resolved value, or `null` when no tooltip should be shown
    ///
    private SLComponent tooltip(SLPropertyValue<T> resolved) {
        if (resolved.tooltip() != null) return SLProperty.copy(resolved.display());
        if (resolved.value() == null || this.tooltipDisplayer == null) return SLComponent.empty();
        return this.tooltipDisplayer.display(resolved.value());
    }
    
    ///
    /// Creates the default display component for a raw property value before custom formatting is applied.
    ///
    /// @param value raw property value
    ///
    /// @return default value display component
    ///
    private SLComponent defaultValueDisplay(T value) {
        if (value instanceof SLDisplayable displayable) return displayable.display();
        if (value instanceof Boolean bool) return SLComponent.enabledDisabled(bool);
        return SLComponent.of(Component.literal(String.valueOf(value)));
    }
    
    ///
    /// Creates a defensive copy of the given component.
    ///
    /// @param component component to copy
    ///
    /// @return copied component, or `null` when the input is `null`
    ///
    private static SLComponent copy(@Nullable Component component) {
        return component != null ? SLComponent.of(component.copy()) : SLComponent.empty();
    }
    
    ///
    /// Fluent builder for composing an {@link SLProperty} definition.
    ///
    /// @param <T> logical property value type
    /// @param <E> block entity type supported by the property
    ///
    public static class SLPropertyBuilder<T, E extends BlockEntity> {
        
        private SLPropertyBuilder() { }
        
        ///
        /// Sets both the stack extractor and updater using a simple raw-value extractor.
        ///
        /// @param extractor extractor used to read values from an {@link ItemStack}
        /// @param updater   updater used to write values to an {@link ItemStack}
        ///
        /// @return the builder instance
        ///
        public SLPropertyBuilder<T, E> stack(Extractor<T, ItemStack> extractor, Updater<T, ItemStack> updater) {
            return this.stackExtractor(extractor).stackUpdater(updater);
        }
        
        ///
        /// Sets both the state resolver and updater using a resolved-value source.
        ///
        /// @param extractor resolver used to read {@link SLPropertyValue} instances from a {@link BlockState}
        /// @param updater   updater used to write logical values to a {@link BlockState}
        ///
        /// @return the builder instance
        ///
        public SLPropertyBuilder<T, E> stateResolved(Extractor<T, BlockState> extractor, StateUpdater<T> updater) {
            return this.stateExtractor(extractor).stateUpdater(updater);
        }
        
        ///
        /// Sets both the block entity extractor and updater using a simple raw-value extractor.
        ///
        /// @param extractor extractor used to read values from the block entity
        /// @param updater   updater used to write values to the block entity
        ///
        /// @return the builder instance
        ///
        public SLPropertyBuilder<T, E> blockEntity(Extractor<T, E> extractor, Updater<T, E> updater) {
            return this.blockEntityExtractor(extractor).blockEntityUpdater(updater);
        }
    }
    
    ///
    /// Reads a property value from a source object.
    ///
    /// @param <T> logical property value type
    /// @param <O> source object type
    ///
    @FunctionalInterface
    public interface Extractor<T, O> {
        ///
        /// Resolves the property value state from the given source object.
        ///
        /// @param object source object
        ///
        /// @return resolved property value
        ///
        SLPropertyValue<T> resolve(O object);
    }
    
    ///
    /// Writes a property value to a mutable target object.
    ///
    /// @param <T> logical property value type
    /// @param <O> target object type
    ///
    @FunctionalInterface
    public interface Updater<T, O> {
        ///
        /// Writes the given property value to the target object.
        ///
        /// @param object target object
        /// @param value  value to apply
        ///
        void update(O object, T value);
    }
    
    ///
    /// Writes a property value to a block state and returns the updated state.
    ///
    /// @param <T> logical property value type
    ///
    @FunctionalInterface
    public interface StateUpdater<T> {
        ///
        /// Writes the given property value to the target block state.
        ///
        /// @param state state to update
        /// @param value value to apply
        ///
        /// @return updated block state
        ///
        BlockState update(BlockState state, T value);
    }
    
    ///
    /// Produces a neighboring property value for directional cycling.
    ///
    /// @param <T> logical property value type
    ///
    @FunctionalInterface
    public interface Cycler<T> {
        ///
        /// Produces the next property value in the given direction.
        ///
        /// @param dir      cycle direction, where `true` means forward and `false` means backward
        /// @param original current property value
        ///
        /// @return cycled property value
        ///
        T cycle(boolean dir, T original);
    }
    
    ///
    /// Formats a raw property value as an {@link SLComponent}.
    ///
    /// @param <T> logical property value type
    ///
    @FunctionalInterface
    public interface Displayer<T> {
        ///
        /// Creates a display component from the given value
        ///
        /// @param value the value to use for display
        ///
        /// @return display component
        ///
        SLComponent display(T value);
    }
    
    ///
    /// Returns a displayer that always emits the same component.
    ///
    /// @param component component to return for every value
    /// @param <T>       logical property value type
    ///
    /// @return constant component displayer
    ///
    public static <T> Displayer<T> directDisplayer(SLComponent component) {
        return _ -> component;
    }
    
    ///
    /// Returns a boolean displayer that chooses between two translation entries.
    ///
    /// @param on  translation used for {@code true}
    /// @param off translation used for {@code false}
    ///
    /// @return boolean value displayer
    ///
    public static Displayer<Boolean> boolDisplayer(SLTranslatable on, SLTranslatable off) {
        return val -> val ? on.translate() : off.translate();
    }
    
    ///
    /// Boolean cycler that toggles between {@code true} and {@code false}.
    ///
    public static final Cycler<Boolean> BOOLEAN_CYCLER = (_, val) -> !val;
    
    ///
    /// Returns a cycler for enum-backed property values.
    ///
    /// @param clazz enum class
    /// @param <E>   enum property type
    ///
    /// @return cycler that wraps around the enum constants in declaration order
    ///
    public static <E extends Enum<E> & PropertyEnum> Cycler<E> enumCycler(Class<E> clazz) {
        return (dir, val) -> {
            E[] values = clazz.getEnumConstants();
            int i = val.ordinal() + (dir ? 1 : -1);
            if (i < 0) i += values.length;
            return values[i % values.length];
        };
    }
    
    ///
    /// Returns a numeric cycler for {@link Short} values within the inclusive range.
    ///
    /// @param min minimum allowed value
    /// @param max maximum allowed value
    ///
    /// @return cycler that wraps around between the given bounds
    ///
    public static Cycler<Short> numberCycler(short min, short max) {
        return (dir, val) -> {
            final short change = (short) (val + (dir ? 1 : -1));
            if (change > max) return min;
            if (change < min) return max;
            return change;
        };
    }
    
    ///
    /// Returns a numeric cycler for {@link Integer} values within the inclusive range.
    ///
    /// @param min minimum allowed value
    /// @param max maximum allowed value
    ///
    /// @return cycler that wraps around between the given bounds
    ///
    public static Cycler<Integer> numberCycler(int min, int max) {
        return (dir, val) -> {
            final int change = val + (dir ? 1 : -1);
            if (change > max) return min;
            if (change < min) return max;
            return change;
        };
    }
    
    ///
    /// Returns a numeric cycler for {@link Long} values within the inclusive range.
    ///
    /// @param min minimum allowed value
    /// @param max maximum allowed value
    ///
    /// @return cycler that wraps around between the given bounds
    ///
    public static Cycler<Long> numberCycler(long min, long max) {
        return (dir, val) -> {
            final long change = val + (dir ? 1 : -1);
            if (change > max) return min;
            if (change < min) return max;
            return change;
        };
    }
    
    ///
    /// Returns a numeric cycler for {@link Float} values within the inclusive range.
    ///
    /// @param min minimum allowed value
    /// @param max maximum allowed value
    ///
    /// @return cycler that wraps around between the given bounds
    ///
    public static Cycler<Float> numberCycler(float min, float max) {
        return (dir, val) -> {
            final float change = val + (dir ? 1 : -1);
            if (change > max) return min;
            if (change < min) return max;
            return change;
        };
    }
    
    ///
    /// Returns a numeric cycler for {@link Double} values within the inclusive range.
    ///
    /// @param min minimum allowed value
    /// @param max maximum allowed value
    ///
    /// @return cycler that wraps around between the given bounds
    ///
    public static Cycler<Double> numberCycler(double min, double max) {
        return (dir, val) -> {
            final double change = val + (dir ? 1 : -1);
            if (change > max) return min;
            if (change < min) return max;
            return change;
        };
    }
}
