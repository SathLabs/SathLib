package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.SathLib;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

///
/// Registry of connected texture predicates keyed by identifier.
///
public final class ConnectedTexturePredicateRegistry {
    
    private static final Map<Identifier, ConnectedTexturePredicate> PREDICATES = new ConcurrentHashMap<>();
    private static final Set<Identifier> MISSING_PREDICATES = ConcurrentHashMap.newKeySet();
    
    private ConnectedTexturePredicateRegistry() { }
    
    ///
    /// Registers a connected texture predicate.
    ///
    /// @param id        identifier used to resolve the predicate
    /// @param predicate predicate implementation to register
    ///
    public static void register(final Identifier id, final ConnectedTexturePredicate predicate) {
        final ConnectedTexturePredicate previous = ConnectedTexturePredicateRegistry.PREDICATES.putIfAbsent(id, predicate);
        
        if (previous != null) {
            throw new IllegalStateException("Duplicate connected texture predicate registration for " + id);
        }
    }
    
    @Nullable
    static ConnectedTexturePredicate resolve(final Identifier id) {
        final ConnectedTexturePredicate predicate = ConnectedTexturePredicateRegistry.PREDICATES.get(id);
        
        if (predicate == null && ConnectedTexturePredicateRegistry.MISSING_PREDICATES.add(id)) {
            SathLib.log.warn("Missing connected texture predicate registration for {}", id);
        }
        
        return predicate;
    }
}
