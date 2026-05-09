package dev.satherov.sathlib.client.model.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;

import org.jspecify.annotations.Nullable;

import java.util.Map;

///
///
///
public interface SLModelPropertyValue {
    
    Codec<? extends SLModelPropertyValue> codec();
    
    StreamCodec<? extends FriendlyByteBuf, ? extends SLModelPropertyValue> streamCodec();
    
    SLModelPropertyFieldHolder asHolder();
    
    default Map<String, String> serializeFields() {
        return this.asHolder().asMap();
    }
    
    default boolean matchesSerializedFields(Map<String, String> fields) {
        return this.asHolder().matches(fields);
    }
    
    default <T> @Nullable SLModelPropertyField<T> getField(String field) {
        return this.asHolder().getField(field);
    }
    
    default <T> @Nullable T getFieldValue(String name) {
        return this.asHolder().get(name);
    }
}
