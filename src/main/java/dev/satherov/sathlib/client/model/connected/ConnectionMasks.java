package dev.satherov.sathlib.client.model.connected;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

import java.util.Map;

///
/// Allows resolving connection masks for block sprites
///
public sealed interface ConnectionMasks permits ConnectionMasks.Empty, ConnectionMasks.Multi, ConnectionMasks.Single {
    
    ///
    /// Creates an empty connection mask
    ///
    /// @return {@link ConnectionMasks.Empty#INSTANCE}
    ///
    static ConnectionMasks empty() {
        return Empty.INSTANCE;
    }
    
    ///
    /// Creates a single connection mask
    ///
    /// @param sprite     texture identifier
    /// @param connection connection mask
    ///
    /// @return single connection mask
    ///
    static ConnectionMasks single(Identifier sprite, ConnectionFaceMasks connection) {
        return new Single(sprite, connection);
    }
    
    ///
    /// Creates a multi-connection mask
    ///
    /// @param connections map of texture identifiers to connection masks
    ///
    /// @return multi-connection mask
    ///
    static ConnectionMasks multi(Map<Identifier, ConnectionFaceMasks> connections) {
        return new Multi(connections);
    }
    
    ///
    /// Gets a single connection mask for the given texture identifier
    ///
    /// @param identifier texture identifier
    ///
    /// @return connection mask
    ///
    @Nullable ConnectionFaceMasks get(Identifier identifier);
    
    enum Empty implements ConnectionMasks {
        INSTANCE;
        
        @Override
        public @Nullable ConnectionFaceMasks get(Identifier identifier) {
            return null;
        }
    }
    
    record Single(Identifier sprite, ConnectionFaceMasks connection) implements ConnectionMasks {
        
        @Override
        public @Nullable ConnectionFaceMasks get(Identifier identifier) {
            return identifier.equals(this.sprite) ? this.connection : null;
        }
    }
    
    record Multi(Map<Identifier, ConnectionFaceMasks> connections) implements ConnectionMasks {
        
        @Override
        public @Nullable ConnectionFaceMasks get(Identifier identifier) {
            return this.connections.get(identifier);
        }
    }
}
