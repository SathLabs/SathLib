package dev.satherov.sathlib.client.model.data;

import dev.satherov.sathlib.SathLib;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SLModelPropertyRegistry {
    
    private static final Map<Identifier, SLModelProperty<?>> PROPERTIES = new ConcurrentHashMap<>();
    private static final Set<Identifier> MISSING_PROPERTIES = ConcurrentHashMap.newKeySet();
    
    ///
    /// Registers a model property.
    ///
    /// @param property property descriptor to register
    ///
    public static void register(final SLModelProperty<?> property) {
        final SLModelProperty<?> previous = SLModelPropertyRegistry.PROPERTIES.putIfAbsent(property.id(), property);
        if (previous != null) throw new IllegalStateException("Duplicate conditional model property registration for " + property.id());
    }
    
    ///
    /// Resolves a model property by identifier.
    ///
    /// @param id property identifier
    ///
    /// @return registered property descriptor, or `null`
    ///
    public static @Nullable SLModelProperty<?> resolve(final Identifier id) {
        final SLModelProperty<?> property = SLModelPropertyRegistry.PROPERTIES.get(id);
        
        if (property == null && SLModelPropertyRegistry.MISSING_PROPERTIES.add(id)) {
            SathLib.log.warn("Missing conditional model property registration for {}", id);
        }
        
        return property;
    }
}
