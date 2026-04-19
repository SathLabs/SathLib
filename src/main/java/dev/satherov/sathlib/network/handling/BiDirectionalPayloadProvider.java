package dev.satherov.sathlib.network.handling;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

///
/// Payload that can be sent from both the client and the server.
///
/// @param <T> payload type handled in both directions
///
@NothingNull
public non-sealed interface BiDirectionalPayloadProvider<T extends SLPayload<T>> extends PayloadProvider<T> {
    
    @Override
    default void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(this.type(), this.codec(), this, this);
    }
    
    @Override
    default void handle(T payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof LocalPlayer player) {
                this.handleClient(payload, context, player);
            } else if (context.player() instanceof ServerPlayer player) {
                this.handleServer(payload, context, player);
            }
        }).whenComplete((_, throwable) -> {
            if (throwable != null) this.exceptionally(payload, context, throwable);
        });
    }
    
    ///
    /// Handles the payload on the server side.
    ///
    /// @param payload Payload to handle
    /// @param context Context of the payload
    /// @param player  {@link ServerPlayer} that received the payload.
    ///
    default void handleServer(T payload, IPayloadContext context, ServerPlayer player) {
        this.handle(payload, context, player);
    }
    
    ///
    /// Handles the payload on the client side.
    ///
    /// @param payload Payload to handle
    /// @param context Context of the payload
    /// @param player  {@link LocalPlayer} that received the payload.
    ///
    default void handleClient(T payload, IPayloadContext context, LocalPlayer player) {
        this.handle(payload, context, player);
    }
    
    ///
    /// Handles the payload on both sides.
    ///
    /// @param payload Payload to handle
    /// @param context Context of the payload
    /// @param player  {@link Player} that received the payload.
    ///
    void handle(T payload, IPayloadContext context, Player player);
}
