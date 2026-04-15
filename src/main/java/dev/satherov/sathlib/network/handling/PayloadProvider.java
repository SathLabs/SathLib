package dev.satherov.sathlib.network.handling;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

///
/// Parent payload provider interface.
///
/// @param <T> Type of the payload
///
@NothingNull
public sealed interface PayloadProvider<T extends SLPayload<T>> extends IPayloadHandler<T> permits ClientPayloadProvider, ServerPayloadProvider, BiDirectionalPayloadProvider {
    
    ///
    /// Registers the payload provider to the given registrar.
    ///
    /// Should be overloaded in each permitted subtype.
    ///
    /// @param registrar Registrar to register the payload provider to
    ///
    /// @see ClientPayloadProvider#register(PayloadRegistrar)
    /// @see ServerPayloadProvider#register(PayloadRegistrar)
    /// @see BiDirectionalPayloadProvider#register(PayloadRegistrar)
    ///
    void register(PayloadRegistrar registrar);
    
    ///
    /// Handles the method in the given context.
    ///
    /// @param payload Payload to handle
    /// @param context Context to handle the payload in
    ///
    @Override
    void handle(T payload, IPayloadContext context);
    
    ///
    /// Type of the payload.
    ///
    /// @return Type of the payload
    ///
    CustomPacketPayload.Type<T> type();
    
    ///
    /// Stream codec for the payload.
    ///
    /// @return Stream codec for the payload
    ///
    StreamCodec<? super RegistryFriendlyByteBuf, T> codec();
    
    ///
    /// Called when the payload processing fails.
    ///
    /// @param payload   Payload that failed to process
    /// @param context   Context in which the payload failed to process
    /// @param throwable Exception that caused the failure
    ///
    default void exceptionally(T payload, IPayloadContext context, Throwable throwable) {
        SathLib.log.error("Error occurred trying to process payload of type '{}'", this.type().id(), throwable);
    }
}
