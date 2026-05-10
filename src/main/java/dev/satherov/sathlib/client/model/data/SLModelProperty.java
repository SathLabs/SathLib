package dev.satherov.sathlib.client.model.data;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

///
/// Registered model-data property descriptor used by SathLib model rules.
///
/// @param <T>      value type stored in the model data
/// @param id       registry identifier of the property
/// @param property runtime NeoForge model-data key
///
public record SLModelProperty<T extends SLModelPropertyValue>(Identifier id, ModelProperty<T> property) {
    
    public static final Codec<SLModelProperty<?>> CODEC = Identifier.CODEC.comapFlatMap(
            id -> {
                final SLModelProperty<?> property = SLModelPropertyRegistry.resolve(id);
                if (property != null) return DataResult.success(property);
                return DataResult.error(() -> "Unknown conditional model property: " + id);
            },
            SLModelProperty::id
    );
    
    ///
    /// Registers a model property with the default permissive predicate.
    ///
    /// @param <T> value type stored in the property
    /// @param id  property identifier
    ///
    /// @return registered property descriptor
    ///
    public static <T extends SLModelPropertyValue> SLModelProperty<T> register(Identifier id) {
        return SLModelProperty.register(id, new ModelProperty<>());
    }
    
    ///
    /// Registers a model property with a custom runtime predicate.
    ///
    /// @param <T>       value type stored in the property
    /// @param id        property identifier
    /// @param predicate runtime validation predicate
    ///
    /// @return registered property descriptor
    ///
    public static <T extends SLModelPropertyValue> SLModelProperty<T> register(Identifier id, Predicate<T> predicate) {
        return SLModelProperty.register(id, new ModelProperty<>(predicate));
    }
    
    private static <T extends SLModelPropertyValue> SLModelProperty<T> register(Identifier id, ModelProperty<T> property) {
        SLModelProperty<T> registered = new SLModelProperty<>(id, property);
        SLModelPropertyRegistry.register(registered);
        return registered;
    }
    
    ///
    /// Reads this property from model data.
    ///
    /// @param data model data to inspect
    ///
    /// @return stored value, or `null`
    ///
    public @Nullable T get(final ModelData data) {
        return data.get(this.property);
    }
}
