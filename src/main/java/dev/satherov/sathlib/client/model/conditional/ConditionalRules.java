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
    public static final Codec<ConditionalPredicate> CODEC = Codec.recursive("conditional", _ -> Identifier.CODEC.partialDispatch(
            "type", rule -> DataResult.success(rule.type()), ConditionalRules::codec
    ));
    
    @ApiStatus.Internal
    public static void init() {
        ModLoader.postEvent(new SLRegisterConditionRulesEvent(ConditionalRules.RULES));
    }
    
    public static ConditionalPredicate always() {
        return Always.INSTANCE;
    }
    
    public static ConditionalPredicate never() {
        return Never.INSTANCE;
    }
    
    public static ConditionalPredicate all(final ConditionalPredicate... predicates) {
        return new All(List.of(predicates));
    }
    
    public static ConditionalPredicate any(final ConditionalPredicate... predicates) {
        return new Any(List.of(predicates));
    }
    
    public static ConditionalPredicate not(final ConditionalPredicate predicate) {
        return new Not(predicate);
    }
    
    public static <T extends Comparable<T>> ConditionalPredicate state(final Property<T> property, final T value) {
        return new State(Map.of(property.getName(), property.getName(value)));
    }
    
    public static ConditionalPredicate state(final UnaryOperator<State.Builder> properties) {
        return new State(properties.apply(new State.Builder()).build());
    }
    
    public static <T extends SLModelPropertyValue> ConditionalPredicate modelProperty(final SLModelProperty<T> property, T value) {
        return new ModelProperties(property, value.serializeFields());
    }
    
    private static <T extends SLModelPropertyValue> @Nullable T resolveModelPropertyValue(final SLModelProperty<T> property, final ModelData data) {
        final T directValue = property.get(data);
        if (directValue != null) return directValue;
        return Mods.FRAMED_BLOCKS.run(() -> FramedBlocksModelDataHelper.resolveModelPropertyValue(property, data));
    }
    
    private static DataResult<? extends MapCodec<? extends ConditionalPredicate>> codec(final Identifier type) {
        final MapCodec<? extends ConditionalPredicate> codec = ConditionalRules.RULES.get(type);
        return codec != null ? DataResult.success(codec) : DataResult.error(() -> "Unknown conditional rule type: " + type);
    }
    
    public enum Always implements ConditionalPredicate {
        INSTANCE;
        
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
    
    public enum Never implements ConditionalPredicate {
        INSTANCE;
        
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
        
        public static class Builder {
            
            private final ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
            
            public <T extends Comparable<T>> Builder put(Property<T> property, T value) {
                this.builder.put(property.getName(), property.getName(value));
                return this;
            }
            
            private Map<String, String> build() {
                return this.builder.build();
            }
        }
    }
    
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
