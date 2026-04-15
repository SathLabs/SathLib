package dev.satherov.sathlib.network.handling;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.server.level.ServerPlayer;

///
/// Payload sent from the client to the server.
///
/// @param <T> Type of the payload.
///
@NothingNull
public non-sealed interface ServerPayloadProvider<T extends SLPayload<T>> extends PayloadProvider<T> {
    
    @Override
    default void register(PayloadRegistrar registrar) {
        registrar.playToServer(this.type(), this.codec(), this);
    }
    
    @Override
    default void handle(T payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                this.handle(payload, context, player);
            }
        }).whenComplete((_, throwable) -> {
            if (throwable != null) this.exceptionally(payload, context, throwable);
        });
    }
    
    ///
    /// Handles the payload on the server.
    ///
    /// @param payload The payload to handle.
    /// @param context The context of the payload.
    /// @param player  The {@link ServerPlayer} that received the payload.
    ///
    void handle(T payload, IPayloadContext context, ServerPlayer player);
}
