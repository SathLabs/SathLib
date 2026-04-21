package dev.satherov.sathlib.client.render.model;

import dev.satherov.sathlib.core.annotations.NothingNull;
import dev.satherov.sathlib.util.SLStringUtils;

import net.neoforged.neoforge.model.data.ModelProperty;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/// Serializable predicate tree used by connected-texture models.
///
/// The same rule type is used both for deciding whether two neighboring
/// blocks connect and for selecting a target variant from the current block
/// state/model-data.
///
/// Rules are serialized into the generated model definition, which keeps the
/// block implementation free from hardcoded client-side interfaces. In
/// practice, that means the same rule objects can be reused both in datagen and
/// at runtime.
@NothingNull
public sealed interface SLConnectedTextureRule permits
        SLAlwaysConnectedTextureRule,
        SLSamePropertiesConnectedTextureRule,
        SLSameModelDataConnectedTextureRule,
        SLStateValueConnectedTextureRule,
        SLModelDataValueConnectedTextureRule,
        SLAndConnectedTextureRule,
        SLOrConnectedTextureRule,
        SLNotConnectedTextureRule {
    
    /// Codec for connected-texture rules.
    ///
    /// The serialized form is a recursive tagged union with a {@code type}
    /// discriminator and rule-specific payload fields.
    Codec<SLConnectedTextureRule> CODEC = Codec.recursive(
            "sl_connected_texture_rule",
            self -> SLConnectedTextureRuleType.CODEC.dispatch(
                    "type",
                    SLConnectedTextureRule::type,
                    type -> type.codec(self))
    );
    
    /// Evaluates this rule.
    ///
    /// For connection checks, {@code current} is the block currently being
    /// rendered and {@code neighbor} is the adjacent block being considered. For
    /// variant selection, callers typically pass the same context for both
    /// parameters and evaluate only predicates that depend on the current
    /// block.
    ///
    /// @param current  currently rendered block context
    /// @param neighbor neighbor block context
    /// @param face     rendered face
    /// @param target   connected-texture target being evaluated
    ///
    /// @return {@code true} when the rule matches
    boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target);
    
    /// Returns this rule's serialized type discriminator.
    ///
    /// This value is consumed by {@link #CODEC} during model
    /// serialization/deserialization.
    ///
    /// @return rule type
    SLConnectedTextureRuleType type();
    
    /// Matches current and neighbor blocks by block identity.
    SLConnectedTextureRule SAME_BLOCK = SLAlwaysConnectedTextureRule.SAME_BLOCK;
    
    /// Matches current and neighbor blocks by exact blockstate identity.
    SLConnectedTextureRule SAME_STATE = SLAlwaysConnectedTextureRule.SAME_STATE;
    
    /// Matches when the target-selected variants are the same for current and
    /// neighbor.
    SLConnectedTextureRule SAME_VARIANT = SLAlwaysConnectedTextureRule.SAME_VARIANT;
}

@NothingNull
enum SLConnectedTextureRuleType implements StringRepresentable {
    ALWAYS(_ -> SLConnectedTextureRuleType.singleton(SLAlwaysConnectedTextureRule.ALWAYS)),
    SAME_BLOCK(_ -> SLConnectedTextureRuleType.singleton(SLAlwaysConnectedTextureRule.SAME_BLOCK)),
    SAME_STATE(_ -> SLConnectedTextureRuleType.singleton(SLAlwaysConnectedTextureRule.SAME_STATE)),
    SAME_PROPERTIES(_ -> Codec.STRING.listOf().fieldOf("properties").xmap(SLSamePropertiesConnectedTextureRule::new, rule -> ((SLSamePropertiesConnectedTextureRule) rule).properties())),
    SAME_MODEL_DATA(_ -> Codec.STRING.listOf().fieldOf("keys").xmap(SLSameModelDataConnectedTextureRule::new, rule -> ((SLSameModelDataConnectedTextureRule) rule).keys())),
    SAME_VARIANT(_ -> SLConnectedTextureRuleType.singleton(SLAlwaysConnectedTextureRule.SAME_VARIANT)),
    STATE_VALUE(_ -> SLConnectedTextureRuleType.stateValueCodec()),
    MODEL_DATA_VALUE(_ -> SLConnectedTextureRuleType.modelDataValueCodec()),
    AND(self -> self.listOf().fieldOf("terms").xmap(SLAndConnectedTextureRule::new, rule -> ((SLAndConnectedTextureRule) rule).terms())),
    OR(self -> self.listOf().fieldOf("terms").xmap(SLOrConnectedTextureRule::new, rule -> ((SLOrConnectedTextureRule) rule).terms())),
    NOT(self -> self.fieldOf("term").xmap(SLNotConnectedTextureRule::new, rule -> ((SLNotConnectedTextureRule) rule).term()));
    
    public static final Codec<SLConnectedTextureRuleType> CODEC = StringRepresentable.fromEnum(SLConnectedTextureRuleType::values);
    
    private final String serializedName;
    private final Function<Codec<SLConnectedTextureRule>, MapCodec<SLConnectedTextureRule>> codecFactory;
    
    SLConnectedTextureRuleType(Function<Codec<SLConnectedTextureRule>, MapCodec<SLConnectedTextureRule>> codecFactory) {
        this.serializedName = SLStringUtils.lower(this.name());
        this.codecFactory = codecFactory;
    }
    
    public MapCodec<SLConnectedTextureRule> codec(Codec<SLConnectedTextureRule> self) {
        return this.codecFactory.apply(self);
    }
    
    private static MapCodec<SLConnectedTextureRule> singleton(SLConnectedTextureRule rule) {
        return MapCodec.unit(rule);
    }
    
    private static MapCodec<SLConnectedTextureRule> stateValueCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("property").forGetter(rule -> ((SLStateValueConnectedTextureRule) rule).property()),
                Codec.STRING.fieldOf("value").forGetter(rule -> ((SLStateValueConnectedTextureRule) rule).value())
        ).apply(instance, SLStateValueConnectedTextureRule::new));
    }
    
    private static MapCodec<SLConnectedTextureRule> modelDataValueCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("key").forGetter(rule -> ((SLModelDataValueConnectedTextureRule) rule).key()),
                Codec.STRING.fieldOf("value").forGetter(rule -> ((SLModelDataValueConnectedTextureRule) rule).value())
        ).apply(instance, SLModelDataValueConnectedTextureRule::new));
    }
    
    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}

@NothingNull
record SLAlwaysConnectedTextureRule(SLConnectedTextureRuleType type) implements SLConnectedTextureRule {
    static final SLAlwaysConnectedTextureRule ALWAYS = new SLAlwaysConnectedTextureRule(SLConnectedTextureRuleType.ALWAYS);
    static final SLAlwaysConnectedTextureRule SAME_BLOCK = new SLAlwaysConnectedTextureRule(SLConnectedTextureRuleType.SAME_BLOCK);
    static final SLAlwaysConnectedTextureRule SAME_STATE = new SLAlwaysConnectedTextureRule(SLConnectedTextureRuleType.SAME_STATE);
    static final SLAlwaysConnectedTextureRule SAME_VARIANT = new SLAlwaysConnectedTextureRule(SLConnectedTextureRuleType.SAME_VARIANT);
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        return switch (this.type) {
            case ALWAYS -> true;
            case SAME_BLOCK -> current.state().getBlock() == neighbor.state().getBlock();
            case SAME_STATE -> current.state() == neighbor.state();
            case SAME_VARIANT -> Objects.equals(target.resolveVariant(current), target.resolveVariant(neighbor));
            default -> throw new IllegalStateException("Unexpected singleton rule type " + this.type);
        };
    }
}

@NothingNull
record SLSamePropertiesConnectedTextureRule(List<String> properties) implements SLConnectedTextureRule {
    
    @Override
    public SLConnectedTextureRuleType type() {
        return SLConnectedTextureRuleType.SAME_PROPERTIES;
    }
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        for (String propertyName : this.properties) {
            Property<?> currentProperty = current.state().getBlock().getStateDefinition().getProperty(propertyName);
            Property<?> neighborProperty = neighbor.state().getBlock().getStateDefinition().getProperty(propertyName);
            if (currentProperty == null || neighborProperty == null) return false;
            if (!Objects.equals(
                    SLSamePropertiesConnectedTextureRule.stringifyStateValue(current.state(), currentProperty),
                    SLSamePropertiesConnectedTextureRule.stringifyStateValue(neighbor.state(), neighborProperty)
            )) return false;
        }
        return true;
    }
    
    private static String stringifyStateValue(BlockState state, Property<?> property) {
        return Objects.toString(state.getValue(property), null);
    }
}

@NothingNull
record SLSameModelDataConnectedTextureRule(List<String> keys) implements SLConnectedTextureRule {
    
    @Override
    public SLConnectedTextureRuleType type() {
        return SLConnectedTextureRuleType.SAME_MODEL_DATA;
    }
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        for (String key : this.keys) {
            ModelProperty<?> property = SLConnectedTextureModelDataKeys.resolve(key);
            if (property == null) return false;
            boolean currentHas = current.modelData().has(property);
            boolean neighborHas = neighbor.modelData().has(property);
            if (currentHas != neighborHas) return false;
            if (currentHas && !Objects.equals(current.modelData().get(property), neighbor.modelData().get(property))) return false;
        }
        return true;
    }
}

@NothingNull
record SLStateValueConnectedTextureRule(String property, String value) implements SLConnectedTextureRule {
    
    @Override
    public SLConnectedTextureRuleType type() {
        return SLConnectedTextureRuleType.STATE_VALUE;
    }
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        Property<?> stateProperty = current.state().getBlock().getStateDefinition().getProperty(this.property);
        if (stateProperty == null) return false;
        return Objects.equals(Objects.toString(current.state().getValue(stateProperty), null), this.value);
    }
}

@NothingNull
record SLModelDataValueConnectedTextureRule(String key, String value) implements SLConnectedTextureRule {
    
    @Override
    public SLConnectedTextureRuleType type() {
        return SLConnectedTextureRuleType.MODEL_DATA_VALUE;
    }
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        ModelProperty<?> property = SLConnectedTextureModelDataKeys.resolve(this.key);
        if (property == null || !current.modelData().has(property)) return false;
        return Objects.equals(Objects.toString(current.modelData().get(property), null), this.value);
    }
}

@NothingNull
record SLAndConnectedTextureRule(List<SLConnectedTextureRule> terms) implements SLConnectedTextureRule {
    
    @Override
    public SLConnectedTextureRuleType type() {
        return SLConnectedTextureRuleType.AND;
    }
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        return this.terms.stream().allMatch(rule -> rule.test(current, neighbor, face, target));
    }
}

@NothingNull
record SLOrConnectedTextureRule(List<SLConnectedTextureRule> terms) implements SLConnectedTextureRule {
    
    @Override
    public SLConnectedTextureRuleType type() {
        return SLConnectedTextureRuleType.OR;
    }
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        return this.terms.stream().anyMatch(rule -> rule.test(current, neighbor, face, target));
    }
}

@NothingNull
record SLNotConnectedTextureRule(SLConnectedTextureRule term) implements SLConnectedTextureRule {
    
    @Override
    public SLConnectedTextureRuleType type() {
        return SLConnectedTextureRuleType.NOT;
    }
    
    @Override
    public boolean test(SLConnectedTextureContext current, SLConnectedTextureContext neighbor, Direction face, SLConnectedTextureTarget target) {
        return !this.term.test(current, neighbor, face, target);
    }
}
