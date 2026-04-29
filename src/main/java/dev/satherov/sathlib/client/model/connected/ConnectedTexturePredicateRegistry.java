package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.SathLib;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectedTexturePredicateRegistry {
    
    private static final Map<Identifier, ConnectedTexturePredicate> PREDICATES = new ConcurrentHashMap<>();
    private static final Set<Identifier> MISSING_PREDICATES = ConcurrentHashMap.newKeySet();
    
    private ConnectedTexturePredicateRegistry() { }
    
    public static void register(final Identifier id, final ConnectedTexturePredicate predicate) {
        final ConnectedTexturePredicate previous = ConnectedTexturePredicateRegistry.PREDICATES.putIfAbsent(id, predicate);
        
        if (previous != null) {
            throw new IllegalStateException("Duplicate connected texture predicate registration for " + id);
        }
    }
    
    public static ConnectedTexturePredicate sameBlock() {
        return context -> context.originState().getBlock() == context.neighborState().getBlock();
    }
    
    public static ConnectedTexturePredicate sameAppearanceBlock() {
        return context -> context.originAppearance().getBlock() == context.neighborAppearance().getBlock();
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
