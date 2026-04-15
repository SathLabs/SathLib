package dev.satherov.sathlib.network.handling;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.client.player.LocalPlayer;

///
/// Payload sent from the server to the client.
///
/// @param <T> Type of the payload.
///
@NothingNull
public non-sealed interface ClientPayloadProvider<T extends SLPayload<T>> extends PayloadProvider<T> {
    
    @Override
    default void register(PayloadRegistrar registrar) {
        registrar.playToClient(this.type(), this.codec(), this);
    }
    
    @Override
    default void handle(T payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof LocalPlayer player) {
                this.handle(payload, context, player);
            }
        }).whenComplete((_, throwable) -> {
            if (throwable != null) this.exceptionally(payload, context, throwable);
        });
    }
    
    ///
    /// Handles the payload on the client.
    ///
    /// @param payload The payload to handle.
    /// @param context The context of the payload.
    /// @param player  The {@link LocalPlayer} that received the payload.
    ///
    void handle(T payload, IPayloadContext context, LocalPlayer player);
}
