package dev.satherov.sathlib.config;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.config.data.ConfigEnum;
import dev.satherov.sathlib.config.data.Range;
import dev.satherov.sathlib.core.mixin.ModConfigSpecEnumValueAccessor;
import dev.satherov.sathlib.util.SLReflectionUtils;
import dev.satherov.sathlib.util.SLResourceUtils;

import net.neoforged.neoforge.common.ModConfigSpec;

import net.minecraft.util.Util;

import com.electronwill.nightconfig.core.EnumGetMethod;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

///
/// Handlers for building the {@link ModConfigSpec} for each type
///
@UtilityClass
public class ConfigHandlers {
    
    ///
    /// Handles the creation of a {@link ModConfigSpec.ConfigValue} for the given field.
    ///
    public static Optional<ModConfigSpec.ConfigValue<?>> create(String mod, ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
        ConfigHandler handler = ConfigHandlers.HANDLERS.get(SLReflectionUtils.getWrapper(field.getType()));
        if (handler == null) return Optional.empty();
        return Optional.of(handler.create(builder.translation(Util.makeDescriptionId("config", SLResourceUtils.id(mod, name))), name, field, object, comments));
    }
    
    ///
    /// All supported config types and their corresponding handlers.
    ///
    private static final Map<Class<?>, ConfigHandler> HANDLERS = ImmutableMap.<Class<?>, ConfigHandler>builder()
            .put(String.class, new StringHandler())
            .put(Boolean.class, new BooleanHandler())
            .put(Integer.class, new IntegerHandler())
            .put(Long.class, new LongHandler())
            .put(Double.class, new DoubleHandler())
            .put(Enum.class, new EnumHandler())
            .put(List.class, new ListHandler())
            .build();
    
    ///
    /// Functional interface for creating a {@link ModConfigSpec.ConfigValue} for a given field.
    ///
    @FunctionalInterface
    private interface ConfigHandler {
        
        ///
        /// Creates a {@link ModConfigSpec.ConfigValue} for the given field.
        ///
        /// @param builder  Builder for the config spec.
        /// @param name     Name of the entry.
        /// @param field    Field to create the config value for.
        /// @param value    Default value for the config entry.
        /// @param comments Comments for the config entry.
        ///
        /// @return Config value from the given field.
        ///
        ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object value, List<String> comments);
    }
    
    ///
    /// Implementation for {@link String} fields.
    ///
    private static class StringHandler implements ConfigHandler {
        
        @Override
        public ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
            String value = ConfigHandlers.validate(name, object, String.class);
            ConfigHandlers.comment(builder, comments, value);
            return builder.define(name, value);
        }
    }
    
    ///
    /// Implementation for {@link Boolean} / `boolean` fields.
    ///
    private static class BooleanHandler implements ConfigHandler {
        
        @Override
        public ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
            boolean value = ConfigHandlers.validate(name, object, Boolean.class);
            ConfigHandlers.comment(builder, comments, value);
            return builder.define(name, value);
        }
    }
    
    ///
    /// Implementation for {@link Integer} / `int` fields.
    ///
    private static class IntegerHandler implements ConfigHandler {
        
        @Override
        public ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
            final MinMax range = MinMax.of(field);
            int value = ConfigHandlers.validate(name, object, Integer.class);
            ConfigHandlers.comment(builder, comments, value);
            return builder.defineInRange(name, value, (int) range.min(), (int) range.max());
        }
    }
    
    ///
    /// Implementation for {@link Long} / `long` fields.
    ///
    private static class LongHandler implements ConfigHandler {
        
        @Override
        public ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
            final MinMax range = MinMax.of(field);
            long value = ConfigHandlers.validate(name, object, Long.class);
            ConfigHandlers.comment(builder, comments, value);
            return builder.defineInRange(name, value, (long) range.min(), (long) range.max());
        }
    }
    
    ///
    /// Implementation for {@link Double} / `double` fields.
    ///
    private static class DoubleHandler implements ConfigHandler {
        
        @Override
        public ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
            final MinMax range = MinMax.of(field);
            double value = ConfigHandlers.validate(name, object, Double.class);
            ConfigHandlers.comment(builder, comments, value);
            return builder.defineInRange(name, value, range.min(), range.max());
        }
    }
    
    ///
    /// Implementation for {@link Enum} fields.
    ///
    @SuppressWarnings("rawtypes")
    private static class EnumHandler implements ConfigHandler {
        
        @Override
        public ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
            Class<? extends Enum> clazz = field.getType().asSubclass(Enum.class);
            Enum<?> value = ConfigHandlers.validate(name, object, clazz);
            if (!(value instanceof ConfigEnum cfg)) {
                throw new IllegalArgumentException("Enum " + clazz.getSimpleName() + " is not a Config Enum");
            }
            
            ConfigHandlers.comment(builder, comments, value);
            Arrays.stream(clazz.getEnumConstants()).forEach(e -> builder.comment(" " + e.name() + ": " + cfg.description()));
            return EnumHandler.defineEnum(name, builder, value, clazz);
        }
        
        ///
        /// Handles the unchecked casts for the enum value.
        ///
        @SuppressWarnings("unchecked")
        private static <V extends Enum<V>> ModConfigSpec.EnumValue<V> defineEnum(String name, ModConfigSpec.Builder builder, Enum<?> value, Class<?> clazz) {
            return EnumHandler.createEnumValue(name, builder, (V) value, (Class<V>) clazz);
        }
        
        ///
        /// Creates a {@link ModConfigSpec.EnumValue} for the given enum value.
        ///
        /// @param name    Name of the enum value.
        /// @param builder Builder for the config.
        /// @param value   Default value of this config entry.
        /// @param clazz   Class of the enum.
        ///
        /// @see ModConfigSpecEnumValueAccessor
        ///
        private static <V extends Enum<V>> ModConfigSpec.EnumValue<V> createEnumValue(String name, ModConfigSpec.Builder builder, V value, Class<V> clazz) {
            Collection<V> allowed = Arrays.asList(clazz.getEnumConstants());
            
            ModConfigSpec.ConfigValue<V> def = builder.define(Collections.singletonList(name), () -> value, obj -> {
                if (clazz.isInstance(obj)) return allowed.contains(clazz.cast(obj));
                try {
                    return allowed.contains(EnumGetMethod.NAME_IGNORECASE.get(obj, clazz));
                } catch (IllegalArgumentException | ClassCastException e) {
                    return false;
                }
            }, clazz);
            
            return ModConfigSpecEnumValueAccessor.create(
                    builder,
                    def.getPath(),
                    () -> value,
                    EnumGetMethod.NAME_IGNORECASE,
                    clazz
            );
        }
    }
    
    ///
    /// Implementation for {@link List} fields.
    ///
    @SuppressWarnings("DataFlowIssue")
    private static class ListHandler implements ConfigHandler {
        
        @Override
        public ModConfigSpec.ConfigValue<?> create(ModConfigSpec.Builder builder, String name, Field field, Object object, List<String> comments) {
            List<?> value = ConfigHandlers.validate(name, object, List.class);
            ConfigHandlers.comment(builder, comments, value);
            return builder.defineList(name, value, null, _ -> true);
        }
    }
    
    ///
    /// Checks to make sure the given object is not null and of the given type.
    ///
    /// @param name   Name of the config entry.
    /// @param object Object to check.
    /// @param clazz  Class of the object.
    ///
    /// @return The given object cast to the given class.
    ///
    private static <T> T validate(String name, Object object, Class<T> clazz) throws IllegalArgumentException, NullPointerException {
        if (object == null) {
            throw new NullPointerException("Config value " + name + " is null");
        }
        
        if (!clazz.isInstance(object)) {
            throw new IllegalArgumentException("Config value " + name + " is not of type " + clazz.getName());
        }
        
        return clazz.cast(object);
    }
    
    ///
    /// Adds the given comments to the config builder.
    ///
    /// @param builder      Builder for the config.
    /// @param comments     Comments to add.
    /// @param defaultValue Default value of the config entry.
    ///
    private static void comment(ModConfigSpec.Builder builder, List<String> comments, Object defaultValue) {
        for (String comment : comments) builder.comment(" " + comment);
        builder.comment(" Default: " + defaultValue);
    }
    
    ///
    /// Small holder for the min and max values of a range.
    ///
    /// @param min Minimum value of the range.
    /// @param max Maximum value of the range.
    ///
    /// @see Range
    ///
    private record MinMax(double min, double max) {
        private static final MinMax DEFAULT = new MinMax(Double.MIN_VALUE, Double.MAX_VALUE);
        
        private static MinMax of(Field field) {
            Range annotation = field.getAnnotation(Range.class);
            if (annotation == null) return MinMax.DEFAULT;
            return new MinMax(annotation.min(), annotation.max());
        }
    }
}
