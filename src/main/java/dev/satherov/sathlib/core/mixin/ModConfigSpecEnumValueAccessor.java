package dev.satherov.sathlib.core.mixin;

import net.neoforged.neoforge.common.ModConfigSpec;

import com.electronwill.nightconfig.core.EnumGetMethod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Supplier;

///
/// Accessor for the internal {@link ModConfigSpec.EnumValue} constructor.
///
@Mixin(ModConfigSpec.EnumValue.class)
public interface ModConfigSpecEnumValueAccessor {
    
    ///
    /// Creates a config enum value instance with NeoForge's internal constructor.
    ///
    /// @param parent          parent builder
    /// @param path            config path
    /// @param defaultSupplier supplier for the default enum value
    /// @param converter       string-to-enum converter
    /// @param clazz           enum class
    /// @param <E>             enum type
    ///
    /// @return constructed enum config value
    ///
    @Invoker("<init>")
    static <E extends Enum<E>> ModConfigSpec.EnumValue<E> create(ModConfigSpec.Builder parent, List<String> path, Supplier<E> defaultSupplier, EnumGetMethod converter, Class<E> clazz) {
        throw new AssertionError();
    }
}
