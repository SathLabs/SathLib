package dev.satherov.sathlib.client.render.model;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Arrays;
import java.util.List;

/// Factory helpers for building connected-texture rule trees.
public final class SLConnectedTextureRules {
    
    private SLConnectedTextureRules() { }
    
    /// Rule that always matches.
    ///
    /// @return always-true rule
    public static SLConnectedTextureRule always() {
        return SLAlwaysConnectedTextureRule.ALWAYS;
    }
    
    /// Rule that matches when current and neighbor are the same block.
    ///
    /// @return same-block rule
    public static SLConnectedTextureRule sameBlock() {
        return SLConnectedTextureRule.SAME_BLOCK;
    }
    
    /// Rule that matches when current and neighbor share the same exact state.
    ///
    /// @return same-state rule
    public static SLConnectedTextureRule sameState() {
        return SLConnectedTextureRule.SAME_STATE;
    }
    
    /// Rule that compares the supplied blockstate properties between current and
    /// neighbor.
    ///
    /// @param properties properties to compare
    ///
    /// @return property-comparison rule
    public static SLConnectedTextureRule sameProperties(Property<?>... properties) {
        return SLConnectedTextureRules.sameProperties(Arrays.stream(properties).map(Property::getName).toList());
    }
    
    /// Rule that compares the supplied property names between current and
    /// neighbor.
    ///
    /// @param properties property names to compare
    ///
    /// @return property-comparison rule
    public static SLConnectedTextureRule sameProperties(String... properties) {
        return SLConnectedTextureRules.sameProperties(List.of(properties));
    }
    
    /// Rule that compares the supplied property names between current and
    /// neighbor.
    ///
    /// @param properties property names to compare
    ///
    /// @return property-comparison rule
    public static SLConnectedTextureRule sameProperties(List<String> properties) {
        return new SLSamePropertiesConnectedTextureRule(List.copyOf(properties));
    }
    
    /// Rule that compares registered model-data keys between current and
    /// neighbor.
    ///
    /// @param keys model-data keys to compare
    ///
    /// @return model-data comparison rule
    public static SLConnectedTextureRule sameModelData(SLConnectedTextureModelDataKey<?>... keys) {
        return SLConnectedTextureRules.sameModelData(Arrays.stream(keys).map(SLConnectedTextureModelDataKey::id).toList());
    }
    
    /// Rule that compares registered model-data ids between current and
    /// neighbor.
    ///
    /// @param keys model-data ids to compare
    ///
    /// @return model-data comparison rule
    public static SLConnectedTextureRule sameModelData(String... keys) {
        return SLConnectedTextureRules.sameModelData(List.of(keys));
    }
    
    /// Rule that compares registered model-data ids between current and
    /// neighbor.
    ///
    /// @param keys model-data ids to compare
    ///
    /// @return model-data comparison rule
    public static SLConnectedTextureRule sameModelData(List<String> keys) {
        return new SLSameModelDataConnectedTextureRule(List.copyOf(keys));
    }
    
    /// Rule that matches when the resolved target variant is the same for
    /// current and neighbor.
    ///
    /// @return same-variant rule
    public static SLConnectedTextureRule sameVariant() {
        return SLConnectedTextureRule.SAME_VARIANT;
    }
    
    /// Rule that matches the current block when a state property has the given
    /// serialized value.
    ///
    /// @param property property to test
    /// @param value    expected serialized value
    ///
    /// @return current-context rule
    public static SLConnectedTextureRule stateValue(Property<?> property, Object value) {
        return SLConnectedTextureRules.stateValue(property.getName(), String.valueOf(value));
    }
    
    /// Rule that matches the current block when a state property has the given
    /// serialized value.
    ///
    /// @param property property name to test
    /// @param value    expected serialized value
    ///
    /// @return current-context rule
    public static SLConnectedTextureRule stateValue(String property, String value) {
        return new SLStateValueConnectedTextureRule(property, value);
    }
    
    /// Rule that matches the current block when a registered model-data key has
    /// the given serialized value.
    ///
    /// @param key   registered model-data key
    /// @param value expected serialized value
    ///
    /// @return current-context rule
    public static SLConnectedTextureRule modelDataValue(SLConnectedTextureModelDataKey<?> key, Object value) {
        return SLConnectedTextureRules.modelDataValue(key.id(), String.valueOf(value));
    }
    
    /// Rule that matches the current block when a registered model-data id has
    /// the given serialized value.
    ///
    /// @param key   registered model-data id
    /// @param value expected serialized value
    ///
    /// @return current-context rule
    public static SLConnectedTextureRule modelDataValue(String key, String value) {
        return new SLModelDataValueConnectedTextureRule(key, value);
    }
    
    /// Logical conjunction.
    ///
    /// @param rules rules to combine
    ///
    /// @return AND-composed rule
    public static SLConnectedTextureRule and(SLConnectedTextureRule... rules) {
        return new SLAndConnectedTextureRule(List.of(rules));
    }
    
    /// Logical disjunction.
    ///
    /// @param rules rules to combine
    ///
    /// @return OR-composed rule
    public static SLConnectedTextureRule or(SLConnectedTextureRule... rules) {
        return new SLOrConnectedTextureRule(List.of(rules));
    }
    
    /// Logical negation.
    ///
    /// @param rule rule to invert
    ///
    /// @return NOT-composed rule
    public static SLConnectedTextureRule not(SLConnectedTextureRule rule) {
        return new SLNotConnectedTextureRule(rule);
    }
}
