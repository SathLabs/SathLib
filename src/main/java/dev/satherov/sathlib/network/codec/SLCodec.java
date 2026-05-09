package dev.satherov.sathlib.network.codec;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

///
/// Shared codecs for common SathLib serialization patterns.
///
@NothingNull
public final class SLCodec {
    
    ///
    /// Codec for {@link UUID} values encoded as strings.
    ///
    public static final Codec<UUID> UUID = Codec.STRING.xmap(java.util.UUID::fromString, java.util.UUID::toString);
    ///
    /// Codec for {@link Instant} values encoded as ISO-8601 strings.
    ///
    public static final Codec<Instant> INSTANT = Codec.STRING.xmap(Instant::parse, Instant::toString);
    
    private SLCodec() { }
    
    ///
    /// Creates a HasMap Codec with the given key and value codecs.
    ///
    /// @param key   Codec for the map keys
    /// @param value Codec for the map values
    /// @param <K>   Type of the map
    /// @param <V>   Type of the map values
    ///
    /// @return codec for a mutable {@link HashMap}
    ///
    public static <K, V> Codec<HashMap<K, V>> map(Codec<K> key, Codec<V> value) {
        return SLCodec.map(key, value, HashMap::new);
    }
    
    ///
    /// Creates a codec for a given map type with the given key and value codecs.
    /// Unlike {@link Codec#unboundedMap(Codec, Codec)}, this allows for a custom map type instead of an immutable map.
    ///
    /// @param key         Codec for the map keys
    /// @param value       Codec for the map values
    /// @param constructor Constructor for the map
    /// @param <K>         Type of the map keys
    /// @param <V>         Type of the map values
    /// @param <T>         Concrete map type produced by the codec
    ///
    /// @return Codec for the given map type
    ///
    public static <K, V, T extends Map<K, V>> Codec<T> map(Codec<K> key, Codec<V> value, Function<Map<K, V>, T> constructor) {
        return Codec.unboundedMap(key, value).xmap(constructor, Function.identity());
    }
    
    ///
    /// Creates a codec for the given enum class.
    ///
    /// @param clazz Enum class
    /// @param <E>   Enum type
    ///
    /// @return Codec for the given enum class
    ///
    public static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Class<E> clazz) {
        return StringRepresentable.fromEnum(clazz::getEnumConstants);
    }
    
    ///
    /// Creates a string codec for the given function. Uses {@link String#valueOf(Object)} for the {@code to} function.
    /// Useful for cases like such as numbers not being allowed as map keys.
    ///
    /// @param converter Function to convert from string to type
    /// @param <T>       Desired type of the codec
    ///
    /// @return Codec for the given type
    ///
    public static <T> Codec<T> stringify(Function<String, T> converter) {
        return SLCodec.stringify(converter, String::valueOf);
    }
    
    ///
    /// Creates a string codec for the given {@code from-to} functions.
    /// Useful for cases like such as numbers not being allowed as map keys.
    ///
    /// @param to   Function to convert from string to type
    /// @param from Function to convert from type to string
    /// @param <T>  Desired type of the codec
    ///
    /// @return Codec for the given type
    ///
    public static <T> Codec<T> stringify(Function<String, T> to, Function<T, String> from) {
        return Codec.STRING.xmap(to, from);
    }
}
