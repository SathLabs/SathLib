package dev.satherov.sathlib.network.codec;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import io.netty.buffer.ByteBuf;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;

@NothingNull
@UtilityClass
public final class SLStreamCodec {
    
    public static final StreamCodec<ByteBuf, UUID> UUID = new StreamCodec<>() {
        public UUID decode(ByteBuf input) {
            return FriendlyByteBuf.readUUID(input);
        }
        
        public void encode(ByteBuf output, UUID value) {
            FriendlyByteBuf.writeUUID(output, value);
        }
    };
    
    public static final StreamCodec<ByteBuf, Instant> INSTANT = StreamCodec.composite(ByteBufCodecs.LONG, Instant::toEpochMilli, Instant::ofEpochMilli);
    
    ///
    /// Creates a hash-map stream codec from the given key-value codecs and map constructor.
    ///
    /// @param keyCodec   Codec for the map keys
    /// @param valueCodec Codec for the map values
    /// @param <K>        Type of the map keys
    /// @param <V>        Type of the map values
    ///
    /// @return Stream codec for the given map type
    ///
    public static <K, V, B extends ByteBuf> StreamCodec<B, HashMap<K, V>> map(StreamCodec<B, K> keyCodec, StreamCodec<B, V> valueCodec) {
        return SLStreamCodec.map(keyCodec, valueCodec, HashMap::new);
    }
    
    ///
    /// Creates a map stream codec from the given key-value codecs and map constructor.
    ///
    /// @param keyCodec    Codec for the map keys
    /// @param valueCodec  Codec for the map values
    /// @param constructor Constructor for the map
    /// @param <K>         Type of the map keys
    /// @param <V>         Type of the map values
    ///
    /// @return Stream codec for the given map type
    ///
    public static <K, V, T extends Map<K, V>, B extends ByteBuf> StreamCodec<B, T> map(StreamCodec<B, K> keyCodec, StreamCodec<B, V> valueCodec, Function<Map<K, V>, T> constructor) {
        return new StreamCodec<>() {
            
            @Override
            public void encode(B output, T value) {
                final int size = value.size();
                output.writeInt(size);
                for (Map.Entry<K, V> entry : value.entrySet()) {
                    keyCodec.encode(output, entry.getKey());
                    valueCodec.encode(output, entry.getValue());
                }
            }
            
            @Override
            public T decode(B input) {
                HashMap<K, V> map = new HashMap<>();
                final int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    K key = keyCodec.decode(input);
                    V value = valueCodec.decode(input);
                    map.put(key, value);
                }
                return constructor.apply(map);
            }
        };
    }
    
    ///
    /// Creates an enum stream codec from the given enum class, clamping out of bounds values.
    ///
    /// @param clazz Enum class
    ///
    /// @return Enum stream codec
    ///
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> forEnum(Class<E> clazz) {
        final IntFunction<E> function = ByIdMap.continuous(E::ordinal, clazz.getEnumConstants(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        return ByteBufCodecs.idMapper(function, E::ordinal);
    }
    
    ///
    /// Creates an enum stream codec from the given enum class and out-of-bounds strategy.
    ///
    /// @param clazz    Enum class
    /// @param strategy Out-of-bounds strategy
    ///
    /// @return Enum stream codec
    ///
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> forEnum(Class<E> clazz, ByIdMap.OutOfBoundsStrategy strategy) {
        final IntFunction<E> function = ByIdMap.continuous(E::ordinal, clazz.getEnumConstants(), strategy);
        return ByteBufCodecs.idMapper(function, E::ordinal);
    }
}
