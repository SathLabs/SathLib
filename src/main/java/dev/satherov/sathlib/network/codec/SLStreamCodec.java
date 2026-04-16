package dev.satherov.sathlib.network.codec;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import io.netty.buffer.ByteBuf;

import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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
    /// Creates an array list stream codec from the given value codec
    ///
    /// @param valueCodec Codec for the list items
    ///
    /// @return Stream codec for the given list type
    ///
    public static <T, B extends ByteBuf> StreamCodec<B, ArrayList<T>> codec(StreamCodec<B, T> valueCodec) {
        return SLStreamCodec.list(valueCodec, ArrayList::new);
    }
    
    /// 
    /// Creates a list stream codec from the given value codec and list supplier
    /// 
    /// @param valueCodec Codec for the list items
    /// @param supplier   Supplier for the list
    /// 
    /// @return Stream codec for the given list type
    /// 
    public static <T, L extends List<T>, B extends ByteBuf> StreamCodec<B, L> list(StreamCodec<B, T> valueCodec, Supplier<L> supplier) {
        return new StreamCodec<>() {
            
            @Override
            public void encode(B output, L value) {
                final int size = value.size();
                output.writeInt(size);
                for (T item : value) {
                    valueCodec.encode(output, item);
                }
            }
            
            @Override
            public L decode(B input) {
               final L list =  supplier.get();
               final int size = list.size();
               for (int i = 0; i < size; i++) {
                   T item = valueCodec.decode(input);
                   list.add(item);
               }
               return list;
            }
        };
    }
    
    ///
    /// Creates a hash-map stream codec from the given key-value codecs
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
    /// Creates a map stream codec from the given key-value codecs and map supplier.
    ///
    /// @param keyCodec    Codec for the map keys
    /// @param valueCodec  Codec for the map values
    /// @param supplier    Supplier for the map
    /// @param <K>         Type of the map keys
    /// @param <V>         Type of the map values
    ///
    /// @return Stream codec for the given map type
    ///
    public static <K, V, M extends Map<K, V>, B extends ByteBuf> StreamCodec<B, M> map(StreamCodec<B, K> keyCodec, StreamCodec<B, V> valueCodec, Supplier<M> supplier) {
        return new StreamCodec<>() {
            
            @Override
            public void encode(B output, M value) {
                final int size = value.size();
                output.writeInt(size);
                for (Map.Entry<K, V> entry : value.entrySet()) {
                    keyCodec.encode(output, entry.getKey());
                    valueCodec.encode(output, entry.getValue());
                }
            }
            
            @Override
            public M decode(B input) {
                final M map = supplier.get();
                final int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    K key = keyCodec.decode(input);
                    V value = valueCodec.decode(input);
                    map.put(key, value);
                }
                return map;
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
        return SLStreamCodec.forEnum(clazz, ByIdMap.OutOfBoundsStrategy.CLAMP);
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
