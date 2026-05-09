package dev.satherov.sathlib.client.model.connected;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;

public interface ConnectionPredicate {
    
    ///
    /// Whether we should connect under the given context.
    ///
    /// @param context connection context
    ///
    /// @return `true` if we should connect
    ///
    boolean shouldConnect(ConnectionContext context);
    
    ///
    /// Identifier of the connection rule.
    ///
    /// @return rule identifier
    ///
    Identifier type();
    
    ///
    /// Codec for the connection rule.
    ///
    /// @return codec for the connection rule
    ///
    MapCodec<? extends ConnectionPredicate> codec();
}
