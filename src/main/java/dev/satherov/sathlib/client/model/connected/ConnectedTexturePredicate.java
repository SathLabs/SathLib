package dev.satherov.sathlib.client.model.connected;

@FunctionalInterface
public interface ConnectedTexturePredicate {
    
    boolean connects(ConnectedTextureContext context);
}
