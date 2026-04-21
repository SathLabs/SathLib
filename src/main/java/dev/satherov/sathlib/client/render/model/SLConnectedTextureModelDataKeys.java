package dev.satherov.sathlib.client.render.model;

import net.neoforged.neoforge.model.data.ModelProperty;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Registry for named model-data properties referenced by connected-texture
/// predicates.
public final class SLConnectedTextureModelDataKeys {
    
    private static final Map<String, ModelProperty<?>> PROPERTIES = new ConcurrentHashMap<>();
    
    private SLConnectedTextureModelDataKeys() { }
    
    /// Registers a named model-data property for use in connected-texture
    /// predicates.
    ///
    /// @param id       stable serialized identifier
    /// @param property model-data property instance
    /// @param <T>      model-data value type
    ///
    /// @return registered key handle
    public static <T> SLConnectedTextureModelDataKey<T> register(String id, ModelProperty<T> property) {
        ModelProperty<?> existing = SLConnectedTextureModelDataKeys.PROPERTIES.putIfAbsent(id, property);
        if (existing != null && existing != property) {
            throw new IllegalStateException("Connected-texture model-data key already registered: " + id);
        }
        return new SLConnectedTextureModelDataKey<>(id, property);
    }
    
    /// Looks up a previously registered model-data property by id.
    ///
    /// @param id serialized property id
    ///
    /// @return registered property or {@code null} when unknown
    public static @Nullable ModelProperty<?> resolve(String id) {
        return SLConnectedTextureModelDataKeys.PROPERTIES.get(id);
    }
}
