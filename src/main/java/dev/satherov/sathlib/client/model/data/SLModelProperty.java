package dev.satherov.sathlib.client.model.data;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public record SLModelProperty<T extends SLModelPropertyValue>(Identifier id, ModelProperty<T> property) {
    
    public static final Codec<SLModelProperty<?>> CODEC = Identifier.CODEC.comapFlatMap(
            id -> {
                final SLModelProperty<?> property = SLModelPropertyRegistry.resolve(id);
                if (property != null) return DataResult.success(property);
                return DataResult.error(() -> "Unknown conditional model property: " + id);
            },
            SLModelProperty::id
    );
    
    public static <T extends SLModelPropertyValue> SLModelProperty<T> register(Identifier id) {
        return SLModelProperty.register(id, new ModelProperty<>());
    }
    
    public static <T extends SLModelPropertyValue> SLModelProperty<T> register(Identifier id, Predicate<T> predicate) {
        return SLModelProperty.register(id, new ModelProperty<>(predicate));
    }
    
    private static <T extends SLModelPropertyValue> SLModelProperty<T> register(Identifier id, ModelProperty<T> property) {
        SLModelProperty<T> registered = new SLModelProperty<>(id, property);
        SLModelPropertyRegistry.register(registered);
        return registered;
    }
    
    public @Nullable T get(final ModelData data) {
        return data.get(this.property);
    }
}
