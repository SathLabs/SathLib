package dev.satherov.sathlib.common.properties;

import lombok.Builder;
import lombok.Getter;

import dev.satherov.sathlib.client.lang.SLTranslatable;
import dev.satherov.sathlib.core.annotations.NothingNull;
import dev.satherov.sathlib.network.chat.SLComponent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

///
/// Represents a property that can be applied to BlockStates, BlockEntities, and ItemStacks and swap between them
///
/// @param <T> property value type
///
@Builder
@NothingNull
@SuppressWarnings({ "doclint:missing", "unused" })
public class BlockItemProperty<T> {
    
    private final @Getter Identifier identifier;
    private final @Getter Class<T> type;
    private final @Getter SLTranslatable name;
    
    private final PropertyCycler<T> cycler;
    
    private final PropertyExtractor<T> itemExtractor;
    private final PropertyApplicator<T, PropertyItemHolder> itemApplicator;
    
    private final PropertyExtractor<T> blockExtractor;
    private final PropertyApplicator<T, PropertyBlockHolder> blockApplicator;
    
    @Builder.Default
    private final PropertyDisplayer<T> valueDisplayer = PropertyDisplayer.defaultValueDisplayer();
    
    @Builder.Default
    private final PropertyDisplayer<T> tooltipDisplayer = PropertyDisplayer.empty();
    
    ///
    /// Creates a new property builder
    ///
    /// @param identifier property identifier
    /// @param type       property value type
    /// @param name       property name
    /// @param <T>        property value type
    ///
    public static <T> BlockItemPropertyBuilder<T> builder(Identifier identifier, Class<T> type, SLTranslatable name) {
        return new BlockItemPropertyBuilder<T>().identifier(identifier).type(type).name(name);
    }
    
    // ===== ===== ===== ===== EXTRACTION ===== ===== ===== =====
    
    ///
    /// Extracts the value of this property from the given ItemStack
    ///
    /// @param stack the ItemStack to extract the value from
    /// @param state the BlockState supplied
    ///
    /// @return the extracted value
    ///
    public T extractValueItem(ItemStack stack, BlockState state) {
        return this.extractValueItem(stack, state, null);
    }
    
    ///
    /// Extracts the value of this property from the given ItemStack
    ///
    /// @param stack  the ItemStack to extract the value from
    /// @param state  the BlockState supplied
    /// @param entity the BlockEntity supplied
    ///
    /// @return the extracted value
    ///
    public T extractValueItem(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        return this.itemExtractor.extract(block, item);
    }
    
    ///
    /// Extracts the value of this property from the given BlockState
    ///
    /// @param stack the ItemStack supplied
    /// @param state the BlockState to extract the value from
    ///
    /// @return the extracted value
    ///
    public T extractValueBlock(ItemStack stack, BlockState state) {
        return this.extractValueBlock(stack, state, null);
    }
    
    ///
    /// Extracts the value of this property from the given BlockState or BlockEntity
    ///
    /// @param stack  the ItemStack supplied
    /// @param state  the BlockState to extract the value from
    /// @param entity the BlockEntity to extract the value from
    ///
    /// @return the extracted value
    ///
    public T extractValueBlock(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        return this.blockExtractor.extract(block, item);
    }
    
    // ===== ===== ===== ===== APPLICATION ===== ===== ===== =====
    
    ///
    /// Applies the value currently present on the BlockState to the ItemStack
    ///
    /// @param stack the ItemStack to apply the value to
    /// @param state the BlockState to apply the value from
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder applyToItem(ItemStack stack, BlockState state) {
        return this.applyToItem(stack, state, null);
    }
    
    ///
    /// Applies the value currently present on the BlockState or BlockEntity to the ItemStack
    ///
    /// @param stack the ItemStack to apply the value to
    /// @param state the BlockState to apply the value from
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder applyToItem(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        
        T original = this.itemExtractor.extract(block, item);
        T value = this.blockExtractor.extract(block, item);
        
        if (Objects.equals(value, original)) return item;
        return this.itemApplicator.apply(block, item, value);
    }
    
    ///
    /// Applies the given value to the ItemStack
    ///
    /// @param value the value to apply
    /// @param stack the ItemStack to apply the value to
    /// @param state the BlockState supplied
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder applyValueItem(T value, ItemStack stack, BlockState state) {
        return this.applyValueItem(value, stack, state, null);
    }
    
    ///
    /// Applies the given value to the ItemStack
    ///
    /// @param value  the value to apply
    /// @param stack  the ItemStack to apply the value to
    /// @param state  the BlockState supplied
    /// @param entity the BlockEntity supplied
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder applyValueItem(T value, ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        return this.itemApplicator.apply(block, item, value);
    }
    
    ///
    /// Applies the value currently present on the ItemStack to the BlockState
    ///
    /// @param stack the ItemStack to apply the value from
    /// @param state the BlockState to apply the value to
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder applyToBlock(ItemStack stack, BlockState state) {
        return this.applyToBlock(stack, state, null);
    }
    
    ///
    /// Applies the value currently present on the ItemStack to the BlockState or BlockEntity
    ///
    /// @param stack  the ItemStack to apply the value from
    /// @param state  the BlockState to apply the value to
    /// @param entity the BlockEntity to apply the value to
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder applyToBlock(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        
        T original = this.blockExtractor.extract(block, item);
        T value = this.itemExtractor.extract(block, item);
        
        if (Objects.equals(value, original)) return block;
        return this.blockApplicator.apply(block, item, value);
    }
    
    ///
    /// Applies the given value to the BlockState
    ///
    /// @param value the value to apply
    /// @param stack the ItemStack supplied
    /// @param state the BlockState to apply the value to
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder applyValueBlock(T value, ItemStack stack, BlockState state) {
        return this.applyValueBlock(value, stack, state, null);
    }
    
    ///
    /// Applies the given value to the BlockState or BlockEntity
    ///
    /// @param value  the value to apply
    /// @param stack  the ItemStack supplied
    /// @param state  the BlockState to apply the value to
    /// @param entity the BlockEntity to apply the value to
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder applyValueBlock(T value, ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        return this.blockApplicator.apply(block, item, value);
    }
    
    // ===== ===== ===== ===== DISPLAY ===== ===== ===== =====
    
    ///
    /// Displays the name and property of this property
    ///
    /// @param value the value to display
    ///
    /// @return the display component
    ///
    public SLComponent displayAll(T value) {
        return SLComponent.empty()
                .append(this.name.translate(ChatFormatting.GRAY))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(this.displayValue(value));
    }
    
    ///
    /// Displays the value currently present on the ItemStack
    ///
    /// @param stack the ItemStack to display the value from
    /// @param state the BlockState supplied
    ///
    /// @return the display component
    ///
    public SLComponent displayItemValue(ItemStack stack, BlockState state) {
        return this.displayValue(this.extractValueItem(stack, state));
    }
    
    ///
    /// Displays the value currently present on the ItemStack
    ///
    /// @param stack  the ItemStack to display the value from
    /// @param state  the BlockState supplied
    /// @param entity the BlockEntity supplied
    ///
    /// @return the display component
    ///
    public SLComponent displayItemValue(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        return this.displayValue(this.extractValueItem(stack, state, entity));
    }
    
    ///
    /// Displays the value currently present on the BlockState
    ///
    /// @param stack the ItemStack supplied
    /// @param state the BlockState to display the value from
    ///
    /// @return the display component
    ///
    public SLComponent displayBlockValue(ItemStack stack, BlockState state) {
        return this.displayValue(this.extractValueBlock(stack, state));
    }
    
    ///
    /// Displays the value currently present on the BlockState or BlockEntity
    ///
    /// @param stack  the ItemStack supplied
    /// @param state  the BlockState to display the value from
    /// @param entity the BlockEntity to display the value from
    ///
    /// @return the display component
    ///
    public SLComponent displayBlockValue(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        return this.displayValue(this.extractValueBlock(stack, state, entity));
    }
    
    ///
    /// Displays the given value
    ///
    /// @param value the value to display
    ///
    /// @return the display component
    ///
    public SLComponent displayValue(T value) {
        return this.valueDisplayer.display(value);
    }
    
    ///
    /// Displays the tooltip of the value currently present on the ItemStack
    ///
    /// @param stack the ItemStack to display the tooltip from
    /// @param state the BlockState supplied
    ///
    /// @return the display component
    ///
    public SLComponent displayItemTooltip(ItemStack stack, BlockState state) {
        return this.displayTooltip(this.extractValueItem(stack, state));
    }
    
    ///
    /// Displays the tooltip of the value currently present on the ItemStack
    ///
    /// @param stack  the ItemStack to display the tooltip from
    /// @param state  the BlockState supplied
    /// @param entity the BlockEntity supplied
    ///
    /// @return the display component
    ///
    public SLComponent displayItemTooltip(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        return this.displayTooltip(this.extractValueItem(stack, state, entity));
    }
    
    ///
    /// Displays the tooltip of the value currently present on the BlockState
    ///
    /// @param stack the ItemStack supplied
    /// @param state the BlockState to display the tooltip from
    ///
    /// @return the display component
    ///
    public SLComponent displayBlockTooltip(ItemStack stack, BlockState state) {
        return this.displayTooltip(this.extractValueBlock(stack, state));
    }
    
    ///
    /// Displays the tooltip of the value currently present on the BlockState or BlockEntity
    ///
    /// @param stack  the ItemStack supplied
    /// @param state  the BlockState to display the tooltip from
    /// @param entity the BlockEntity to display the tooltip from
    ///
    /// @return the display component
    ///
    public SLComponent displayBlockTooltip(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        return this.displayTooltip(this.extractValueBlock(stack, state, entity));
    }
    
    ///
    /// Displays the tooltip of the given value
    ///
    /// @param value the value to display
    ///
    /// @return the display component
    ///
    public SLComponent displayTooltip(T value) {
        return this.tooltipDisplayer.display(value);
    }
    
    // ===== ===== ===== ===== CYCLE ===== ===== ===== =====
    
    ///
    /// Cycles the value present on the ItemStack in the given direction
    ///
    /// @param forward whether to cycle forward or backward
    /// @param stack   the ItemStack to cycle the value from
    /// @param state   the BlockState supplied
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder cycleItem(boolean forward, ItemStack stack, BlockState state) {
        return this.cycleItem(forward, stack, state, null);
    }
    
    ///
    /// Cycles the value present on the ItemStack in the given direction
    ///
    /// @param forward whether to cycle forward or backward
    /// @param stack   the ItemStack to cycle the value from
    /// @param state   the BlockState supplied
    /// @param entity  the BlockEntity supplied
    ///
    /// @return the modified ItemStack
    ///
    public PropertyItemHolder cycleItem(boolean forward, ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        T original = this.itemExtractor.extract(block, item);
        T updated = this.cycler.cycle(forward, original);
        if (Objects.equals(updated, original)) return item;
        return this.applyValueItem(updated, stack, state, entity);
    }
    
    ///
    /// Cycles the value present on the BlockState in the given direction
    ///
    /// @param forward whether to cycle forward or backward
    /// @param stack   the ItemStack supplied
    /// @param state   the BlockState to cycle the value from
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder cycleBlock(boolean forward, ItemStack stack, BlockState state) {
        return this.cycleBlock(forward, stack, state, null);
    }
    
    ///
    /// Cycles the value present on the BlockState in the given direction
    ///
    /// @param forward whether to cycle forward or backward
    /// @param stack   the ItemStack supplied
    /// @param state   the BlockState to cycle the value from
    ///
    /// @return the modified BlockState
    ///
    public PropertyBlockHolder cycleBlock(boolean forward, ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        T original = this.blockExtractor.extract(block, item);
        T updated = this.cycler.cycle(forward, original);
        if (Objects.equals(updated, original)) return block;
        return this.applyValueBlock(updated, stack, state, entity);
    }
    
    // ===== ===== ===== ===== MATCHING ===== ===== ===== =====
    
    
    ///
    /// Checks whether the two given ItemStack match
    ///
    /// @param stack the first ItemStack
    /// @param other the second ItemStack
    /// @param state the BlockState supplied
    ///
    /// @return `true` if the two ItemStacks match, `false` otherwise
    ///
    public boolean matchItems(ItemStack stack, ItemStack other, BlockState state) {
        return this.matchItems(stack, other, state, null);
    }
    
    ///
    /// Checks whether the two given ItemStack match
    ///
    /// @param stack  the first ItemStack
    /// @param other  the second ItemStack
    /// @param state  the BlockState supplied
    /// @param entity the BlockEntity supplied
    ///
    /// @return `true` if the two ItemStacks match, `false` otherwise
    ///
    public boolean matchItems(ItemStack stack, ItemStack other, BlockState state, @Nullable BlockEntity entity) {
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        final PropertyItemHolder stackHolder = new PropertyItemHolder(stack);
        final PropertyItemHolder otherHolder = new PropertyItemHolder(other);
        
        final T stackValue = this.itemExtractor.extract(block, stackHolder);
        final T otherValue = this.itemExtractor.extract(block, otherHolder);
        
        return Objects.equals(stackValue, otherValue);
    }
    
    ///
    /// Checks whether the two given BlockState match
    ///
    /// @param stack the ItemStack supplied
    /// @param state the first BlockState
    /// @param other the second BlockState
    ///
    /// @return `true` if the two BlockStates match, `false` otherwise
    ///
    public boolean matchBlocks(ItemStack stack, BlockState state, BlockState other) {
        return this.matchBlocks(stack, state, null, other, null);
    }
    
    ///
    /// Checks whether the two given BlockState and BlockEntities match
    ///
    /// @param stack       the ItemStack supplied
    /// @param state       the first BlockState
    /// @param entity      the first BlockEntity
    /// @param other       the second BlockState
    /// @param otherEntity the second BlockEntity
    ///
    /// @return `true` if the two BlockStates match, `false` otherwise
    ///
    public boolean matchBlocks(ItemStack stack, BlockState state, @Nullable BlockEntity entity, BlockState other, @Nullable BlockEntity otherEntity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder stateHolder = new PropertyBlockHolder(state, entity);
        final PropertyBlockHolder otherHolder = new PropertyBlockHolder(other, otherEntity);
        
        final T stateValue = this.blockExtractor.extract(stateHolder, item);
        final T otherValue = this.blockExtractor.extract(otherHolder, item);
        
        return Objects.equals(stateValue, otherValue);
    }
    
    ///
    /// Checks whether the given ItemStack matches the given BlockState
    ///
    /// @param stack the ItemStack to check
    /// @param state the BlockState to check
    ///
    /// @return `true` if the ItemStack matches the BlockState, `false` otherwise
    ///
    public boolean match(ItemStack stack, BlockState state) {
        return this.match(stack, state, null);
    }
    
    ///
    /// Checks whether the given ItemStack matches the given BlockState and BlockEntity
    ///
    /// @param stack the ItemStack to check
    /// @param state the BlockState to check
    ///
    /// @return `true` if the ItemStack matches the BlockState, `false` otherwise
    ///
    public boolean match(ItemStack stack, BlockState state, @Nullable BlockEntity entity) {
        final PropertyItemHolder item = new PropertyItemHolder(stack);
        final PropertyBlockHolder block = new PropertyBlockHolder(state, entity);
        
        final T stateValue = this.blockExtractor.extract(block, item);
        final T itemValue = this.itemExtractor.extract(block, item);
        
        return Objects.equals(stateValue, itemValue);
    }
    
    public static final class BlockItemPropertyBuilder<T> {
        
        public BlockItemPropertyBuilder<T> item(PropertyExtractor<T> extractor, PropertyApplicator<T, PropertyItemHolder> applicator) {
            return this.itemExtractor(extractor).itemApplicator(applicator);
        }
        
        public BlockItemPropertyBuilder<T> block(PropertyExtractor<T> extractor, PropertyApplicator<T, PropertyBlockHolder> applicator) {
            return this.blockExtractor(extractor).blockApplicator(applicator);
        }
    }
}
