package dev.satherov.sathlib.client.model.conditional;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.client.model.data.SLModelProperty;
import dev.satherov.sathlib.client.model.data.SLModelPropertyValue;
import dev.satherov.sathlib.compat.Mods;
import dev.satherov.sathlib.compat.framedblocks.FramedBlocksModelDataHelper;
import dev.satherov.sathlib.core.event.client.SLRegisterConditionRulesEvent;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

///
/// Utility class for all default conditional rules.
///
@UtilityClass
public class ConditionalRules {
    
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
    /// Identifier of the `state` rule.
    ///
    public static final Identifier STATE = SathLib.id("state");
    ///
    /// Identifier of the `model_property` rule.
    ///
    public static final Identifier MODEL_PROPERTY = SathLib.id("model_property");
    
    private static final Map<Identifier, MapCodec<? extends ConditionalPredicate>> RULES = new ConcurrentHashMap<>();
    
    ///
    /// Codec for all conditional rules.
    ///
    public static final Codec<ConditionalPredicate> CODEC = Codec.recursive("conditional", _ -> Identifier.CODEC.partialDispatch(
            "type", rule -> DataResult.success(rule.type()), ConditionalRules::codec
    ));
    
    ///
    /// Initializes the conditional rules.
    ///
    @ApiStatus.Internal
    public static void init() {
        ModLoader.postEvent(new SLRegisterConditionRulesEvent(ConditionalRules.RULES));
    }
    
    ///
    /// Returns a predicate that always matches.
    ///
    /// @return {@link Always#INSTANCE}
    ///
    public static ConditionalPredicate always() {
        return Always.INSTANCE;
    }
    
    ///
    /// Returns a predicate that never matches.
    ///
    /// @return {@link Never#INSTANCE}
    ///
    public static ConditionalPredicate never() {
        return Never.INSTANCE;
    }
    
    ///
    /// Returns a predicate that matches if all the given predicates match.
    ///
    /// @param predicates the predicates to check
    ///
    /// @return {@link All#All(List)}
    ///
    public static ConditionalPredicate all(final ConditionalPredicate... predicates) {
        return new All(List.of(predicates));
    }
    
    ///
    /// Returns a predicate that matches if any of the given predicates match.
    ///
    /// @param predicates the predicates to check
    ///
    /// @return {@link Any#Any(List)}
    ///
    public static ConditionalPredicate any(final ConditionalPredicate... predicates) {
        return new Any(List.of(predicates));
    }
    
    ///
    /// Returns a predicate that matches if the given predicate does not match.
    ///
    /// @param predicate the predicate to check
    ///
    /// @return {@link Not#Not(ConditionalPredicate)}
    ///
    public static ConditionalPredicate not(final ConditionalPredicate predicate) {
        return new Not(predicate);
    }
    
    ///
    /// Returns a predicate that matches if the given block state matches.
    ///
    /// @param <T>      block-state property value type
    /// @param property the property to check
    /// @param value    the value to check
    ///
    /// @return {@link State#State(Map)}
    ///
    public static <T extends Comparable<T>> ConditionalPredicate state(final Property<T> property, final T value) {
        return new State(Map.of(property.getName(), property.getName(value)));
    }
    
    ///
    /// Returns a predicate that matches if the given block state matches.
    ///
    /// @param operator the operator to apply to the builder
    ///
    /// @return {@link State#State(Map)}
    ///
    public static ConditionalPredicate state(final UnaryOperator<State.Builder> operator) {
        return new State(operator.apply(new State.Builder()).build());
    }
    
    ///
    /// Returns a predicate that matches if the given model property matches.
    ///
    /// @param <T>      model property value type
    /// @param property the property to check
    /// @param value    the value to check
    ///
    /// @return {@link ModelProperties#ModelProperties(SLModelProperty, Map)}
    ///
    public static <T extends SLModelPropertyValue> ConditionalPredicate modelProperty(final SLModelProperty<T> property, T value) {
        return new ModelProperties(property, value.serializeFields());
    }
    
    ///
    /// Resolves the given model property value.
    /// If the property is not present in the model data, it will attempt to resolve it in a Framed Block.
    ///
    /// @param property the property to resolve
    ///
    /// @return the resolved value, or `null` if the property is not present in the model data
    ///
    private static <T extends SLModelPropertyValue> @Nullable T resolveModelPropertyValue(final SLModelProperty<T> property, final ModelData data) {
        final T directValue = property.get(data);
        if (directValue != null) return directValue;
        return Mods.FRAMED_BLOCKS.run(() -> FramedBlocksModelDataHelper.resolveModelPropertyValue(property, data));
    }
    
    ///
    /// Returns a codec for the given conditional rule type.
    ///
    /// @param type the rule type
    ///
    /// @return the codec for the rule type, or an error if the type is not registered
    ///
    private static DataResult<? extends MapCodec<? extends ConditionalPredicate>> codec(final Identifier type) {
        final MapCodec<? extends ConditionalPredicate> codec = ConditionalRules.RULES.get(type);
        return codec != null ? DataResult.success(codec) : DataResult.error(() -> "Unknown conditional rule type: " + type);
    }
    
    ///
    /// Predicate that always matches
    ///
    public enum Always implements ConditionalPredicate {
        ///
        /// The `always` predicate instance
        ///
        INSTANCE;
        
        ///
        /// Map codec for the `always` predicate
        ///
        public static final MapCodec<Always> CODEC = MapCodec.unit(Always.INSTANCE);
        
        @Override
        public boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
            return true;
        }
        
        @Override
        public Identifier type() {
            return ConditionalRules.ALWAYS;
        }
        
        @Override
        public MapCodec<Always> codec() {
            return Always.CODEC;
        }
    }
    
    ///
    /// Predicate that never matches
    ///
    public enum Never implements ConditionalPredicate {
        ///
        /// The `never` predicate instance
        ///
        INSTANCE;
        
        ///
        /// Map codec for the `never` predicate
        ///
        public static final MapCodec<Never> CODEC = MapCodec.unit(Never.INSTANCE);
        
        
        @Override
        public boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
            return false;
        }
        
        @Override
        public Identifier type() {
            return ConditionalRules.NEVER;
        }
        
        @Override
        public MapCodec<Never> codec() {
            return Never.CODEC;
        }
    }
    
    ///
    /// Predicate that matches only when every nested predicate matches.
    ///
    /// @param rules predicates to evaluate
    ///
    public record All(List<ConditionalPredicate> rules) implements ConditionalPredicate {
        
        public static final MapCodec<All> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(ConditionalRules.CODEC).fieldOf("rules").forGetter(All::rules)
        ).apply(instance, All::new));
        
        @Override
        public boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
            for (final ConditionalPredicate rule : this.rules) {
                if (!rule.matches(getter, pos, state, data)) return false;
            }
            return true;
        }
        
        @Override
        public Identifier type() {
            return ConditionalRules.ALL;
        }
        
        @Override
        public MapCodec<All> codec() {
            return All.CODEC;
        }
    }
    
    ///
    /// Predicate that matches when at least one nested predicate matches.
    ///
    /// @param rules predicates to evaluate
    ///
    public record Any(List<ConditionalPredicate> rules) implements ConditionalPredicate {
        
        public static final MapCodec<Any> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.list(ConditionalRules.CODEC).fieldOf("rules").forGetter(Any::rules)
        ).apply(instance, Any::new));
        
        @Override
        public boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
            for (final ConditionalPredicate rule : this.rules) {
                if (rule.matches(getter, pos, state, data)) return true;
            }
            return false;
        }
        
        @Override
        public Identifier type() {
            return ConditionalRules.ANY;
        }
        
        @Override
        public MapCodec<Any> codec() {
            return Any.CODEC;
        }
    }
    
    ///
    /// Predicate that inverts another predicate.
    ///
    /// @param rule predicate to negate
    ///
    public record Not(ConditionalPredicate rule) implements ConditionalPredicate {
        
        public static final MapCodec<Not> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ConditionalRules.CODEC.fieldOf("rule").forGetter(Not::rule)
        ).apply(instance, Not::new));
        
        @Override
        public boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
            return !this.rule.matches(getter, pos, state, data);
        }
        
        @Override
        public Identifier type() {
            return ConditionalRules.NOT;
        }
        
        @Override
        public MapCodec<Not> codec() {
            return Not.CODEC;
        }
    }
    
    ///
    /// Predicate that matches a set of serialized block-state properties.
    ///
    /// @param states serialized property-name to value map
    ///
    public record State(Map<String, String> states) implements ConditionalPredicate {
        
        public static final MapCodec<State> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("states").forGetter(State::states)
        ).apply(instance, State::new));
        
        @Override
        public boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
            final StateDefinition<Block, BlockState> definition = state.getBlock().getStateDefinition();
            for (final Map.Entry<String, String> entry : this.states.entrySet()) {
                final Property<?> property = definition.getProperty(entry.getKey());
                if (property == null) return false;
                if (property.getValue(entry.getValue()).filter(val -> state.getValue(property).equals(val)).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public Identifier type() {
            return ConditionalRules.STATE;
        }
        
        @Override
        public MapCodec<State> codec() {
            return State.CODEC;
        }
        
        ///
        /// Builder for serialized state-property predicates.
        ///
        public static class Builder {
            
            private final ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
            
            private Builder() { }
            
            ///
            /// Adds one required state-property value to the predicate.
            ///
            /// @param <T>      block-state property value type
            /// @param property property to serialize
            /// @param value    required property value
            ///
            /// @return this builder
            ///
            public <T extends Comparable<T>> Builder put(Property<T> property, T value) {
                this.builder.put(property.getName(), property.getName(value));
                return this;
            }
            
            private Map<String, String> build() {
                return this.builder.build();
            }
        }
    }
    
    ///
    /// Predicate that matches serialized model-property fields.
    ///
    /// @param property model property to inspect
    /// @param values   serialized field-name to value map
    ///
    public record ModelProperties(SLModelProperty<?> property, Map<String, String> values) implements ConditionalPredicate {
        
        public static final MapCodec<ModelProperties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SLModelProperty.CODEC.fieldOf("property").forGetter(ModelProperties::property),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("values").forGetter(ModelProperties::values)
        ).apply(instance, ModelProperties::new));
        
        @Override
        public boolean matches(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
            final SLModelPropertyValue value = ConditionalRules.resolveModelPropertyValue(this.property, data);
            if (value == null) return false;
            return value.matchesSerializedFields(this.values);
        }
        
        @Override
        public Identifier type() {
            return ConditionalRules.MODEL_PROPERTY;
        }
        
        @Override
        public MapCodec<ModelProperties> codec() {
            return ModelProperties.CODEC;
        }
    }
}
