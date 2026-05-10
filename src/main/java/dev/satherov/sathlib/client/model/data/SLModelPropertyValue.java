package dev.satherov.sathlib.client.model.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;

import org.jspecify.annotations.Nullable;

import java.util.Map;

///
/// Marker interface that all type values of a {@link SLModelProperty} must implement.
///
public interface SLModelPropertyValue {
    
    ///
    /// Codec for serializing and deserializing this value.
    ///
    /// @return Codec for this value.
    ///
    Codec<? extends SLModelPropertyValue> codec();
    
    ///
    /// Stream codec for serializing and deserializing this value.
    ///
    /// @return Stream codec for this value.
    ///
    StreamCodec<? extends FriendlyByteBuf, ? extends SLModelPropertyValue> streamCodec();
    
    ///
    /// Returns this value as a map holder of values.
    ///
    /// @return map holder of values.
    ///
    SLModelPropertyFieldHolder asHolder();
    
    ///
    /// Serializes this value into a map of string field names and values.
    ///
    /// @return map of string field names and values.
    ///
    default Map<String, String> serializeFields() {
        return this.asHolder().asMap();
    }
    
    ///
    /// Checks if this value matches the given map of string field names and values.
    ///
    /// @param fields map of string field names and values.
    ///
    /// @return `true` if this value matches the given map of string field names and values, `false` otherwise.
    ///
    default boolean matchesSerializedFields(Map<String, String> fields) {
        return this.asHolder().matches(fields);
    }
    
    ///
    /// Gets the field with the given name.
    ///
    /// @param <T>   field value type
    /// @param field name of the field or `null` if not found.
    ///
    /// @return field descriptor, or `null` if not found
    ///
    default <T> @Nullable SLModelPropertyField<T> getField(String field) {
        return this.asHolder().getField(field);
    }
    
    ///
    /// Gets the value of the field with the given name.
    ///
    /// @param <T>  field value type
    /// @param name name of the field.
    ///
    /// @return field value, or `null` if not found
    ///
    default <T> @Nullable T getFieldValue(String name) {
        return this.asHolder().get(name);
    }
}
