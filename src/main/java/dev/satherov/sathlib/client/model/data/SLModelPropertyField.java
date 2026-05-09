package dev.satherov.sathlib.client.model.data;

import net.minecraft.network.codec.StreamCodec;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.netty.buffer.ByteBuf;


public record SLModelPropertyField<T>(
        String name,
        Codec<T> codec,
        StreamCodec<? extends ByteBuf, T> streamCodec
) {
    public DataResult<T> deserialize(final String value) {
        if (value == null) return DataResult.error(() -> "Cannot deserialize null value for model property field " + this.name);
        
        try {
            return this.codec.parse(JsonOps.INSTANCE, JsonParser.parseString(value));
        } catch (final JsonSyntaxException _) {
            return this.codec.parse(JsonOps.INSTANCE, new JsonPrimitive(value));
        }
    }
    
    public boolean matchesSerialized(final T value, final String serializedValue) {
        return this.deserialize(serializedValue)
                .result()
                .filter(value::equals)
                .isPresent();
    }
    
    public String serialize(final T value) {
        return this.codec.encodeStart(JsonOps.INSTANCE, value)
                .result()
                .map(JsonElement::toString)
                .orElseThrow(() -> new IllegalStateException("Failed to serialize model property field " + this.name + " with value " + value));
    }
    
    @SuppressWarnings("unchecked")
    String serializeUnchecked(final Object value) {
        return this.serialize((T) value);
    }
    
    boolean matchesSerializedUnchecked(final Object value, final String serializedValue) {
        return this.deserialize(serializedValue)
                .result()
                .filter(value::equals)
                .isPresent();
    }
}
