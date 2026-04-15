package dev.satherov.sathlib.network.handling;


import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

///
/// Payload sent via the network. Should have an inner handler class implementing a {@link PayloadProvider}
/// as well as a private constructor with {@link RegistryFriendlyByteBuf} or an extending class as parameter
/// to use in the stream codec
///
/// @param <T> Type of the payload.
///
public interface SLPayload<T extends SLPayload<T>> extends CustomPacketPayload {
    
    ///
    /// Creates a new stream codec with the given encoders and decoders.
    ///
    /// @param encoder Encoder for the payload. Should be {@code ExamplePayload::encode}
    /// @param decoder Decoder for the payload. Should be {@code ExamplePayload::new}
    /// @param <B>     Buffer type
    /// @param <T>     Payload type
    ///
    /// @return Stream codec.
    ///
    static <B extends RegistryFriendlyByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> encoder, StreamDecoder<B, T> decoder) {
        return StreamCodec.ofMember(encoder, decoder);
    }
    
    ///
    /// Creates a new payload type with the given identifier.
    ///
    /// @param identifier Identifier for the payload type.
    /// @param <R>        Type of the payload.
    ///
    /// @return Payload type.
    ///
    static <R extends SLPayload<R>> Type<R> type(final Identifier identifier) {
        return new Type<>(identifier);
    }
    
    ///
    /// Type of the payload. Gives a unique identifier for the payload.
    ///
    /// @return Type of the payload.
    ///
    @Override
    @NonNull
    Type<T> type();
    
    ///
    /// Encodes the payload into the provided buffer.
    ///
    /// @param buf Buffer to encode into.
    /// @param <R> Type of the buffer.
    ///
    <R extends RegistryFriendlyByteBuf> void encode(R buf);
}
