package dev.satherov.sathlib.client.model.connected;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.client.model.data.SLModelProperty;
import dev.satherov.sathlib.client.model.data.SLModelPropertyField;
import dev.satherov.sathlib.client.model.data.SLModelPropertyValue;
import dev.satherov.sathlib.compat.Mods;
import dev.satherov.sathlib.compat.framedblocks.FramedBlocksModelDataHelper;
import dev.satherov.sathlib.core.event.client.SLRegisterConnectionRulesEvent;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

///
/// Utility class for the built-in connected-texture connection rules.
///
@UtilityClass
public class ConnectionRules {
    
    ///
    /// Identifier of the `always` rule.
    ///
    public static final Identifier ALWAYS = SathLib.id("always");
    ///
    /// Identifier of the `never` rule.
    ///
    public static final Identifier NEVER = SathLib.id("never");
    ///
    /// Identifier of the `all` rule.
    ///
    public static final Identifier ALL = SathLib.id("all");
    ///
    /// Identifier of the `any` rule.
    ///
    public static final Identifier ANY = SathLib.id("any");
    ///
    /// Identifier of the `not` rule.
    ///
    public static final Identifier NOT = SathLib.id("not");
    ///
    /// Identifier of the `same_block` rule.
    ///
    public static final Identifier SAME_BLOCK = SathLib.id("same_block");
    ///
    /// Identifier of the `same_state` rule.
    ///
    public static final Identifier SAME_STATE = SathLib.id("same_state");
    ///
    /// Identifier of the `same_state_property` rule.
    ///
    public static final Identifier SAME_STATE_PROPERTY = SathLib.id("same_state_property");
    ///
    /// Identifier of the `origin_state_property` rule.
    ///
    public static final Identifier ORIGIN_STATE_PROPERTY = SathLib.id("origin_state_property");
    ///
    /// Identifier of the `neighbor_state_property` rule.
    ///
    public static final Identifier NEIGHBOR_STATE_PROPERTY = SathLib.id("neighbor_state_property");
    ///
    /// Identifier of the `same_model_property` rule.
    ///
    public static final Identifier SAME_MODEL_PROPERTY = SathLib.id("same_model_property");
    ///
    /// Identifier of the `origin_model_property` rule.
    ///
    public static final Identifier ORIGIN_MODEL_PROPERTY = SathLib.id("origin_model_property");
    ///
    /// Identifier of the `neighbour_model_property` rule.
    ///
    public static final Identifier NEIGHBOUR_MODEL_PROPERTY = SathLib.id("neighbour_model_property");
    private static final Map<Identifier, MapCodec<? extends ConnectionPredicate>> RULES = new ConcurrentHashMap<>();
    ///
    /// Codec for all registered connection rules.
    ///
    public static final Codec<ConnectionPredicate> CODEC = Codec.recursive("connected_texture", _ -> Identifier.CODEC.partialDispatch(
            "type", rule -> DataResult.success(rule.type()), ConnectionRules::codec
    ));
    
    ///
    /// Initializes the built-in rule registrations.
    ///
    @ApiStatus.Internal
    public static void init() {
        ModLoader.postEvent(new SLRegisterConnectionRulesEvent(ConnectionRules.RULES));
    }
    
    ///
    /// The `always` rule. Will always connect.
    ///
    /// @return {@link Always#INSTANCE}
    ///
    public static ConnectionPredicate always() {
        return Always.INSTANCE;
    }
    
    ///
    /// The `never` rule. Will never connect.
    ///
    /// @return {@link Never#INSTANCE}
    ///
    public static ConnectionPredicate never() {
        return Never.INSTANCE;
    }
    
    ///
    /// The `all` rule. Will connect if all the given rules connect.
    ///
    /// @param rules the rules to check
    ///
    /// @return {@link All#All(List)}
    ///
    public static ConnectionPredicate all(final ConnectionPredicate... rules) {
        return new All(List.of(rules));
    }
    
    ///
    /// The `any` rule. Will connect if any of the given rules connect.
    ///
    /// @param rules the rules to check
    ///
    /// @return {@link Any#Any(List)}
    ///
    public static ConnectionPredicate any(final ConnectionPredicate... rules) {
        return new Any(List.of(rules));
    }
    
    ///
    /// The `not` rule. Will connect if the given rule does not connect.
    ///
    /// @param rule the rule to check
    ///
    /// @return {@link Not#Not(ConnectionPredicate)}
    ///
    public static ConnectionPredicate not(final ConnectionPredicate rule) {
        return new Not(rule);
    }
    
    ///
    /// The `same_block` rule. Will connect if the origin and neighbor blocks are the same.
    ///
    /// @return {@link SameBlock#INSTANCE}
    ///
    /// @see ConnectionRules#sameBlock()
    ///
    public static ConnectionPredicate sameBlock() {
        return SameBlock.INSTANCE;
    }
    
    ///
    /// The `same_state` rule. Will connect if the origin and neighbor have the same block state.
    ///
    /// @return {@link SameState#INSTANCE}
    ///
    public static ConnectionPredicate sameState() {
        return SameState.INSTANCE;
    }
    
    ///
    /// The `same_state_property` rule. Will connect if the origin and neighbor have the same value for the given block state property.
    ///
    /// @param property the property to check
    ///
    /// @return {@link SameStateProperty#SameStateProperty(String)}
    ///
    public static ConnectionPredicate sameStateProperty(final Property<?> property) {
        return new SameStateProperty(property.getName());
    }
    
    ///
    /// The `origin_state_property` rule. Will connect if the origin state has the given value for the given block state property.
    ///
    /// @param <T>      block-state property value type
    /// @param property the property to check
    /// @param value    the value to check
    ///
    /// @return {@link OriginStateProperty#OriginStateProperty(String, String)}
    ///
    public static <T extends Comparable<T>> ConnectionPredicate originStateProperty(final Property<T> property, final T value) {
        return new OriginStateProperty(property.getName(), property.getName(value));
    }
    
    ///
    /// The `neighbor_state_property` rule. Will connect if the neighbor state has the given value for the given block state property.
    ///
    /// @param <T>      block-state property value type
    /// @param property the property to check
    /// @param value    the value to check
    ///
    /// @return {@link NeighborStateProperty#NeighborStateProperty(String, String)}
    ///
    public static <T extends Comparable<T>> ConnectionPredicate neighborStateProperty(final Property<T> property, final T value) {
        return new NeighborStateProperty(property.getName(), property.getName(value));
    }
    
    ///
    /// The `same_model_property` rule. Will connect if the origin and neighbor have the same value for the given model property.
    ///
    /// @param property the property to check
    /// @param field    the field to check
    ///
    /// @return {@link SameModelProperty#SameModelProperty(SLModelProperty, String)}
    ///
    public static ConnectionPredicate sameModelProperty(final SLModelProperty<?> property, SLModelPropertyField<?> field) {
        return new SameModelProperty(property, field.name());
    }
    
    ///
    /// The `origin_model_property` rule. Will connect if the origin model has the given value for the given model property.
    ///
    /// @param <T>      model field value type
    /// @param property the property to check
    /// @param field    the field to check
    /// @param value    the value to check
    ///
    /// @return {@link OriginModelProperty#OriginModelProperty(SLModelProperty, String, String)}
    ///
    public static <T> ConnectionPredicate originModelProperty(SLModelProperty<?> property, SLModelPropertyField<T> field, T value) {
        return new OriginModelProperty(property, field.name(), field.serialize(value));
    }
    
    ///
    /// The `neighbour_model_property` rule. Will connect if the origin model has the given value for the given model property.
    ///
    /// @param <T>      model field value type
    /// @param property the property to check
    /// @param field    the field to check
    /// @param value    the value to check
    ///
    /// @return {@link NeighbourModelProperty#NeighbourModelProperty(SLModelProperty, String, String)}
    ///
    public static <T> ConnectionPredicate neighbourModelProperty(SLModelProperty<?> property, SLModelPropertyField<T> field, T value) {
        return new NeighbourModelProperty(property, field.name(), field.serialize(value));
    }
    
    private static DataResult<? extends MapCodec<? extends ConnectionPredicate>> codec(final Identifier type) {
        final MapCodec<? extends ConnectionPredicate> codec = ConnectionRules.RULES.get(type);
        return codec != null ? DataResult.success(codec) : DataResult.error(() -> "Unknown connected texture rule type: " + type);
    }
    
    private static <T extends Comparable<T>> boolean matchingBlockProperty(Property<T> property, BlockState first, BlockState second) {
        T firstValue = first.getValue(property);
        T secondValue = second.getValue(property);
        return firstValue.equals(secondValue);
    }
    
    private static <T extends SLModelPropertyValue> @Nullable T resolveModelPropertyValue(final SLModelProperty<T> property, final ModelData data) {
        final T directValue = property.get(data);
        if (directValue != null) return directValue;
        return Mods.FRAMED_BLOCKS.run(() -> FramedBlocksModelDataHelper.resolveModelPropertyValue(property, data));
    }
    
    private static <T> boolean matchesSerializedFieldValue(final SLModelPropertyField<T> field, final SLModelPropertyValue value, final String fieldName, final String serializedValue) {
        final T actualValue = value.getFieldValue(fieldName);
        return actualValue != null && field.matchesSerialized(actualValue, serializedValue);
    }
    
    ///
    /// Rule that always connects.
    ///
    public enum Always implements ConnectionPredicate {
        ///
        /// Singleton instance of the rule.
        ///
        INSTANCE;
        
        ///
        /// Codec for the rule.
        ///
        public static final MapCodec<Always> CODEC = MapCodec.unit(Always.INSTANCE);
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            return true;
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.ALWAYS;
        }
        
        @Override
        public MapCodec<Always> codec() {
            return Always.CODEC;
        }
    }
    
    ///
    /// Rule that never connects.
    ///
    public enum Never implements ConnectionPredicate {
        ///
        /// Singleton instance of the rule.
        ///
        INSTANCE;
        
        ///
        /// Codec for the rule.
        ///
        public static final MapCodec<Never> CODEC = MapCodec.unit(Never.INSTANCE);
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            return false;
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.NEVER;
        }
        
        @Override
        public MapCodec<Never> codec() {
            return Never.CODEC;
        }
    }
    
    ///
    /// Rule that connects only when both blocks are the same block type.
    ///
    public enum SameBlock implements ConnectionPredicate {
        ///
        /// Singleton instance of the rule.
        ///
        INSTANCE;
        
        ///
        /// Codec for the rule.
        ///
        public static final MapCodec<SameBlock> CODEC = MapCodec.unit(SameBlock.INSTANCE);
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            return context.originState().getBlock() == context.neighborState().getBlock();
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.SAME_BLOCK;
        }
        
        @Override
        public MapCodec<SameBlock> codec() {
            return SameBlock.CODEC;
        }
    }
    
    ///
    /// Rule that connects only when all compared state properties match.
    ///
    public enum SameState implements ConnectionPredicate {
        ///
        /// Singleton instance of the rule.
        ///
        INSTANCE;
        
        ///
        /// Codec for the rule.
        ///
        public static final MapCodec<SameState> CODEC = MapCodec.unit(SameState.INSTANCE);
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            final BlockState originState = context.originState();
            final BlockState neighborState = context.neighborState();
            
            final Set<Property<?>> properties = new HashSet<>(originState.getProperties());
            properties.addAll(neighborState.getProperties());
            
            for (final Property<?> property : properties) {
                if (!ConnectionRules.matchingBlockProperty(property, originState, neighborState)) return false;
            }
            
            return true;
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.SAME_STATE;
        }
        
        @Override
        public MapCodec<SameState> codec() {
            return SameState.CODEC;
        }
    }
    
    ///
    /// Rule that requires every nested rule to connect.
    ///
    /// @param rules nested rules to evaluate
    ///
    public record All(List<ConnectionPredicate> rules) implements ConnectionPredicate {
        
        public static final MapCodec<All> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(ConnectionRules.CODEC).fieldOf("rules").forGetter(All::rules)
        ).apply(instance, All::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            for (final ConnectionPredicate rule : this.rules) {
                if (!rule.shouldConnect(context)) return false;
            }
            return true;
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.ALL;
        }
        
        @Override
        public MapCodec<All> codec() {
            return All.CODEC;
        }
    }
    
    ///
    /// Rule that requires at least one nested rule to connect.
    ///
    /// @param rules nested rules to evaluate
    ///
    public record Any(List<ConnectionPredicate> rules) implements ConnectionPredicate {
        
        public static final MapCodec<Any> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(ConnectionRules.CODEC).fieldOf("rules").forGetter(Any::rules)
        ).apply(instance, Any::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            for (final ConnectionPredicate rule : this.rules) {
                if (rule.shouldConnect(context)) return true;
            }
            return false;
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.ANY;
        }
        
        @Override
        public MapCodec<Any> codec() {
            return Any.CODEC;
        }
    }
    
    ///
    /// Rule that negates another rule.
    ///
    /// @param rule rule to negate
    ///
    public record Not(ConnectionPredicate rule) implements ConnectionPredicate {
        
        public static final MapCodec<Not> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ConnectionRules.CODEC.fieldOf("rule").forGetter(Not::rule)
        ).apply(instance, Not::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            return !this.rule.shouldConnect(context);
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.NOT;
        }
        
        @Override
        public MapCodec<? extends ConnectionPredicate> codec() {
            return Not.CODEC;
        }
    }
    
    ///
    /// Rule that compares one named state property between origin and neighbor.
    ///
    /// @param property serialized property name
    ///
    public record SameStateProperty(String property) implements ConnectionPredicate {
        
        public static final MapCodec<SameStateProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(SameStateProperty::property)
        ).apply(instance, SameStateProperty::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            final BlockState originState = context.originState();
            final StateDefinition<Block, BlockState> originDefinition = originState.getBlock().getStateDefinition();
            final BlockState neighborState = context.neighborState();
            final StateDefinition<Block, BlockState> neighborDefinition = neighborState.getBlock().getStateDefinition();
            
            final Property<?> originProperty = originDefinition.getProperty(this.property);
            final Property<?> neighborProperty = neighborDefinition.getProperty(this.property);
            if (originProperty == null || neighborProperty == null) return false;
            
            return ConnectionRules.matchingBlockProperty(originProperty, originState, neighborState);
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.SAME_STATE_PROPERTY;
        }
        
        @Override
        public MapCodec<SameStateProperty> codec() {
            return SameStateProperty.CODEC;
        }
    }
    
    ///
    /// Rule that checks a serialized state-property value on the origin block.
    ///
    /// @param property serialized property name
    /// @param value    serialized property value
    ///
    public record OriginStateProperty(String property, String value) implements ConnectionPredicate {
        
        public static final MapCodec<OriginStateProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(OriginStateProperty::property),
                Codec.STRING.fieldOf("value").forGetter(OriginStateProperty::value)
        ).apply(instance, OriginStateProperty::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            final BlockState state = context.originState();
            final StateDefinition<Block, BlockState> definition = state.getBlock().getStateDefinition();
            
            final Property<?> stateProperty = definition.getProperty(this.property);
            if (stateProperty == null) return false;
            
            return stateProperty.getValue(this.value).filter(value -> state.getValue(stateProperty).equals(value)).isPresent();
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.ORIGIN_STATE_PROPERTY;
        }
        
        @Override
        public MapCodec<OriginStateProperty> codec() {
            return OriginStateProperty.CODEC;
        }
    }
    
    ///
    /// Rule that checks a serialized state-property value on the neighbor block.
    ///
    /// @param property serialized property name
    /// @param value    serialized property value
    ///
    public record NeighborStateProperty(String property, String value) implements ConnectionPredicate {
        
        public static final MapCodec<NeighborStateProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(NeighborStateProperty::property),
                Codec.STRING.fieldOf("value").forGetter(NeighborStateProperty::value)
        ).apply(instance, NeighborStateProperty::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            final BlockState state = context.neighborState();
            final StateDefinition<Block, BlockState> definition = state.getBlock().getStateDefinition();
            
            final Property<?> stateProperty = definition.getProperty(this.property);
            if (stateProperty == null) return false;
            
            return stateProperty.getValue(this.value).filter(value -> state.getValue(stateProperty).equals(value)).isPresent();
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.NEIGHBOR_STATE_PROPERTY;
        }
        
        @Override
        public MapCodec<NeighborStateProperty> codec() {
            return NeighborStateProperty.CODEC;
        }
    }
    
    ///
    /// Rule that compares one model-property field between origin and neighbor.
    ///
    /// @param property model property to inspect
    /// @param field    field name to compare
    ///
    public record SameModelProperty(SLModelProperty<?> property, String field) implements ConnectionPredicate {
        
        public static final MapCodec<SameModelProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SLModelProperty.CODEC.fieldOf("property").forGetter(SameModelProperty::property),
                Codec.STRING.fieldOf("field").forGetter(SameModelProperty::field)
        ).apply(instance, SameModelProperty::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            final ModelData originState = context.originData();
            final ModelData neighborState = context.neighborData();
            
            final SLModelPropertyValue originValue = ConnectionRules.resolveModelPropertyValue(this.property, originState);
            final SLModelPropertyValue neighborValue = ConnectionRules.resolveModelPropertyValue(this.property, neighborState);
            if (originValue == null || neighborValue == null) return false;
            
            return Objects.equals(originValue.getFieldValue(this.field), neighborValue.getFieldValue(this.field));
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.SAME_MODEL_PROPERTY;
        }
        
        @Override
        public MapCodec<SameModelProperty> codec() {
            return SameModelProperty.CODEC;
        }
    }
    
    ///
    /// Rule that checks one serialized model-property field value on the origin block.
    ///
    /// @param property model property to inspect
    /// @param field    field name to compare
    /// @param value    serialized field value
    ///
    public record OriginModelProperty(SLModelProperty<?> property, String field, String value) implements ConnectionPredicate {
        
        public static final MapCodec<OriginModelProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SLModelProperty.CODEC.fieldOf("property").forGetter(OriginModelProperty::property),
                Codec.STRING.fieldOf("field").forGetter(OriginModelProperty::field),
                Codec.STRING.fieldOf("value").forGetter(OriginModelProperty::value)
        ).apply(instance, OriginModelProperty::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            final ModelData state = context.originData();
            
            final SLModelPropertyValue value = ConnectionRules.resolveModelPropertyValue(this.property, state);
            if (value == null) return false;
            
            final SLModelPropertyField<?> field = value.getField(this.field);
            if (field == null) return false;
            
            return ConnectionRules.matchesSerializedFieldValue(field, value, this.field, this.value);
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.ORIGIN_MODEL_PROPERTY;
        }
        
        @Override
        public MapCodec<OriginModelProperty> codec() {
            return OriginModelProperty.CODEC;
        }
    }
    
    ///
    /// Rule that checks one serialized model-property field value on the neighbor block.
    ///
    /// @param property model property to inspect
    /// @param field    field name to compare
    /// @param value    serialized field value
    ///
    public record NeighbourModelProperty(SLModelProperty<?> property, String field, String value) implements ConnectionPredicate {
        
        public static final MapCodec<NeighbourModelProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SLModelProperty.CODEC.fieldOf("property").forGetter(NeighbourModelProperty::property),
                Codec.STRING.fieldOf("field").forGetter(NeighbourModelProperty::field),
                Codec.STRING.fieldOf("value").forGetter(NeighbourModelProperty::value)
        ).apply(instance, NeighbourModelProperty::new));
        
        @Override
        public boolean shouldConnect(ConnectionContext context) {
            final ModelData state = context.neighborData();
            
            final SLModelPropertyValue value = ConnectionRules.resolveModelPropertyValue(this.property, state);
            if (value == null) return false;
            
            final SLModelPropertyField<?> field = value.getField(this.field);
            if (field == null) return false;
            
            return ConnectionRules.matchesSerializedFieldValue(field, value, this.field, this.value);
        }
        
        @Override
        public Identifier type() {
            return ConnectionRules.NEIGHBOUR_MODEL_PROPERTY;
        }
        
        @Override
        public MapCodec<NeighbourModelProperty> codec() {
            return NeighbourModelProperty.CODEC;
        }
    }
}
