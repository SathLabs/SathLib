package dev.satherov.sathlib.client.model.connected;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jspecify.annotations.Nullable;

import java.util.List;

///
/// Factory methods for common connected texture rules.
///
public final class SLConnectedTextureRules {
    
    private SLConnectedTextureRules() { }
    
    ///
    /// Returns a rule that always connects.
    ///
    /// @return always-true rule
    ///
    public static SLConnectedTextureRule always() {
        return Always.INSTANCE;
    }
    
    ///
    /// Returns a rule that never connects.
    ///
    /// @return always-false rule
    ///
    public static SLConnectedTextureRule never() {
        return Never.INSTANCE;
    }
    
    ///
    /// Returns a rule that connects only when every supplied rule connects.
    ///
    /// @param rules rules to evaluate
    ///
    /// @return conjunction rule
    ///
    public static SLConnectedTextureRule all(final SLConnectedTextureRule... rules) {
        return new All(List.of(rules));
    }
    
    ///
    /// Returns a rule that connects when any supplied rule connects.
    ///
    /// @param rules rules to evaluate
    ///
    /// @return disjunction rule
    ///
    public static SLConnectedTextureRule any(final SLConnectedTextureRule... rules) {
        return new Any(List.of(rules));
    }
    
    ///
    /// Returns a rule that negates the supplied rule.
    ///
    /// @param rule rule to negate
    ///
    /// @return negated rule
    ///
    public static SLConnectedTextureRule not(final SLConnectedTextureRule rule) {
        return new Not(rule);
    }
    
    ///
    /// Returns a rule that connects blocks of the same block type.
    ///
    /// @return same-block rule
    ///
    public static SLConnectedTextureRule sameBlock() {
        return SameBlock.INSTANCE;
    }
    
    ///
    /// Returns a rule that connects blocks with the same rendered appearance.
    ///
    /// @return same-appearance rule
    ///
    public static SLConnectedTextureRule sameAppearanceBlock() {
        return SameAppearanceBlock.INSTANCE;
    }
    
    ///
    /// Returns a rule that connects when both states share the same value for the supplied property.
    ///
    /// @param property property that must match
    /// @param <T>      property value type
    ///
    /// @return same-state rule
    ///
    public static <T extends Comparable<T>> SLConnectedTextureRule sameState(final Property<T> property) {
        return new SameState(property.getName());
    }
    
    ///
    /// Returns a rule that connects when the origin state matches the supplied property value.
    ///
    /// @param property property to test on the origin state
    /// @param value    expected property value
    /// @param <T>      property value type
    ///
    /// @return origin-state rule
    ///
    public static <T extends Comparable<T>> SLConnectedTextureRule originState(final Property<T> property, final T value) {
        return new OriginState(property.getName(), property.getName(value));
    }
    
    ///
    /// Returns a rule that connects when the neighbor state matches the supplied property value.
    ///
    /// @param property property to test on the neighbor state
    /// @param value    expected property value
    /// @param <T>      property value type
    ///
    /// @return neighbor-state rule
    ///
    public static <T extends Comparable<T>> SLConnectedTextureRule neighborState(final Property<T> property, final T value) {
        return new NeighborState(property.getName(), property.getName(value));
    }
    
    enum Always implements SLConnectedTextureRule {
        INSTANCE;
        
        static final MapCodec<Always> CODEC = MapCodec.unit(Always.INSTANCE);
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return true;
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_ALWAYS;
        }
    }
    
    enum Never implements SLConnectedTextureRule {
        INSTANCE;
        
        static final MapCodec<Never> CODEC = MapCodec.unit(Never.INSTANCE);
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return false;
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_NEVER;
        }
    }
    
    record All(List<SLConnectedTextureRule> rules) implements SLConnectedTextureRule {
        
        static MapCodec<All> CODEC(final Codec<SLConnectedTextureRule> self) {
            return self.listOf().fieldOf("rules").xmap(All::new, All::rules);
        }
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            for (final SLConnectedTextureRule rule : this.rules) {
                if (!rule.connects(context)) return false;
            }
            
            return true;
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_ALL;
        }
    }
    
    record Any(List<SLConnectedTextureRule> rules) implements SLConnectedTextureRule {
        
        static MapCodec<Any> CODEC(final Codec<SLConnectedTextureRule> self) {
            return self.listOf().fieldOf("rules").xmap(Any::new, Any::rules);
        }
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            for (final SLConnectedTextureRule rule : this.rules) {
                if (rule.connects(context)) return true;
            }
            
            return false;
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_ANY;
        }
    }
    
    record Not(SLConnectedTextureRule rule) implements SLConnectedTextureRule {
        
        static MapCodec<Not> CODEC(final Codec<SLConnectedTextureRule> self) {
            return self.fieldOf("rule").xmap(Not::new, Not::rule);
        }
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return !this.rule.connects(context);
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_NOT;
        }
    }
    
    enum SameBlock implements SLConnectedTextureRule {
        INSTANCE;
        
        static final MapCodec<SameBlock> CODEC = MapCodec.unit(SameBlock.INSTANCE);
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return context.originState().getBlock() == context.neighborState().getBlock();
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_SAME_BLOCK;
        }
    }
    
    enum SameAppearanceBlock implements SLConnectedTextureRule {
        INSTANCE;
        
        static final MapCodec<SameAppearanceBlock> CODEC = MapCodec.unit(SameAppearanceBlock.INSTANCE);
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return context.originAppearance().getBlock() == context.neighborAppearance().getBlock();
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_SAME_APPEARANCE_BLOCK;
        }
    }
    
    record SameState(String property) implements SLConnectedTextureRule {
        
        static final MapCodec<SameState> CODEC = Codec.STRING.fieldOf("property").xmap(SameState::new, SameState::property);
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return SLConnectedTextureRules.sameState(context.originState(), context.neighborState(), this.property);
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_SAME_STATE;
        }
    }
    
    record OriginState(String property, String value) implements SLConnectedTextureRule {
        
        static final MapCodec<OriginState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(OriginState::property),
                Codec.STRING.fieldOf("value").forGetter(OriginState::value)
        ).apply(instance, OriginState::new));
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return SLConnectedTextureRules.hasState(context.originState(), this.property, this.value);
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_ORIGIN_STATE;
        }
    }
    
    record NeighborState(String property, String value) implements SLConnectedTextureRule {
        
        static final MapCodec<NeighborState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(NeighborState::property),
                Codec.STRING.fieldOf("value").forGetter(NeighborState::value)
        ).apply(instance, NeighborState::new));
        
        @Override
        public boolean connects(final ConnectedTextureContext context) {
            return SLConnectedTextureRules.hasState(context.neighborState(), this.property, this.value);
        }
        
        @Override
        public net.minecraft.resources.Identifier type() {
            return SLConnectedTextureRule.TYPE_NEIGHBOR_STATE;
        }
    }
    
    private static boolean sameState(final BlockState originState, final BlockState neighborState, final String propertyName) {
        
        
        final Property<?> originProperty = SLConnectedTextureRules.property(originState, propertyName);
        final Property<?> neighborProperty = SLConnectedTextureRules.property(neighborState, propertyName);
        if (originProperty == null || neighborProperty == null) return false;
        return SLConnectedTextureRules.propertyValueName(originState, originProperty).equals(SLConnectedTextureRules.propertyValueName(neighborState, neighborProperty));
    }
    
    private static boolean hasState(final BlockState state, final String propertyName, final String valueName) {
        final Property<?> property = SLConnectedTextureRules.property(state, propertyName);
        
        if (property == null) {
            return false;
        }
        
        return SLConnectedTextureRules.hasState(state, property, valueName);
    }
    
    @Nullable
    private static Property<?> property(final BlockState state, final String propertyName) {
        return state.getBlock().getStateDefinition().getProperty(propertyName);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean hasState(final BlockState state, final Property<?> property, final String valueName) {
        return ((Property) property).getValue(valueName)
                .filter(value -> state.getValue((Property) property).equals(value))
                .isPresent();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String propertyValueName(final BlockState state, final Property<?> property) {
        return ((Property) property).getName(state.getValue((Property) property));
    }
}
