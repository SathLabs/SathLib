package dev.satherov.sathlib.client.model.connected;

///
/// Predicate used to decide whether two blocks should connect for a connected texture.
///
@FunctionalInterface
public interface ConnectedTexturePredicate {
    
    ///
    /// Returns whether the origin block should connect to the neighbor described by the context.
    ///
    /// @param context connected texture evaluation context
    ///
    /// @return `true` when the blocks should connect
    ///
    boolean connects(ConnectedTextureContext context);
}
