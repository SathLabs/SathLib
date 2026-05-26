package dev.satherov.sathlib.config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import dev.satherov.sathlib.config.data.Config;
import dev.satherov.sathlib.config.data.ConfigEntry;
import dev.satherov.sathlib.config.data.ConfigEnum;
import dev.satherov.sathlib.config.data.ConfigHolder;
import dev.satherov.sathlib.config.data.Group;
import dev.satherov.sathlib.util.SLReflectionUtils;
import dev.satherov.sathlib.util.SLStringUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforgespi.language.ModFileScanData;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

///
/// Static config discovery and reload integration for SathLib mods.
///
@Slf4j
@UtilityClass
public class SLConfigLoader {
    
    private static final Type CONFIG_HOLDER = Type.getType(ConfigHolder.class);
    ///
    /// {@link VarHandle} for {@link FMLModContainer#scanResults}
    ///
    private static final VarHandle SCAN_RESULTS = SLReflectionUtils.findVarHandle(FMLModContainer.class, ModFileScanData.class, "scanResults");
    ///
    /// {@link VarHandle} for {@link FMLModContainer#layer}
    ///
    private static final VarHandle LAYER = SLReflectionUtils.findVarHandle(FMLModContainer.class, Module.class, "layer");
    private static final Set<String> HOOKED_MODS = ConcurrentHashMap.newKeySet();
    private static final Map<String, Map<ModConfigSpec, Cache>> CACHE = new ConcurrentHashMap<>();
    
    ///
    /// Generates the config file for the given mod container. Should be called within the mod constructor.
    ///
    /// @param container Mod container to generate the config for.
    ///
    public static void discover(FMLModContainer container) {
        SLConfigLoader.hook(container);
        
        ModFileScanData scanResults = SLConfigLoader.getScanResults(container);
        Module layer = SLConfigLoader.getLayer(container);
        if (scanResults == null || layer == null) {
            SLConfigLoader.throwOrLog("Failed to get mod scan results or module layer for " + container.getModId());
            return;
        }
        
        for (ModFileScanData.AnnotationData annotation : scanResults.getAnnotations()) {
            if (!annotation.annotationType().equals(SLConfigLoader.CONFIG_HOLDER)) continue;
            
            String name = annotation.clazz().getClassName();
            Class<?> clazz = Class.forName(layer, name);
            if (clazz == null) {
                SLConfigLoader.throwOrLog("Failed to load config holder class " + name);
                continue;
            }
            
            SLConfigLoader.generate(container, clazz);
        }
    }
    
    ///
    /// Hooks the config up to the mod event bus.
    ///
    /// @param container Mod container to hook the config for.
    ///
    private static void hook(FMLModContainer container) {
        String modId = container.getModId();
        if (!SLConfigLoader.HOOKED_MODS.add(modId)) return; // Already hooked, nothing to do
        
        IEventBus bus = container.getEventBus();
        if (bus == null) {
            SLConfigLoader.throwOrLog("No event bus available for " + modId);
            return;
        }
        
        Consumer<ModConfigEvent> executor = event -> {
            ModConfig config = event.getConfig();
            
            if (!config.getModId().equalsIgnoreCase(modId)) return;
            if (!(config.getSpec() instanceof ModConfigSpec spec)) return;
            
            Map<ModConfigSpec, Cache> caches = SLConfigLoader.CACHE.get(config.getModId());
            if (caches == null) {
                SLConfigLoader.log.debug("No cache available for {}", modId);
                return;
            }
            
            Cache cache = caches.get(spec);
            if (cache == null) {
                SLConfigLoader.throwOrLog("No config cache found for '" + config.getFileName() + "'");
                return;
            }
            
            cache.values().forEach((field, value) -> {
                try {
                    field.setAccessible(true);
                    field.set(null, value.get());
                } catch (Throwable e) {
                    SLConfigLoader.log.error("Failed to update config value for {}", field, e);
                }
            });
        };
        
        bus.addListener(ModConfigEvent.Loading.class, executor::accept);
        bus.addListener(ModConfigEvent.Reloading.class, executor::accept);
    }
    
    ///
    /// Generates the config files contained within the {@link ConfigHolder} class.
    ///
    /// @param container Mod container to generate the config for.
    /// @param holder    Config holder class to generate the configs from.
    ///
    private static void generate(FMLModContainer container, Class<?> holder) {
        final String modId = container.getModId();
        final Set<ModConfig.Type> types = new HashSet<>();
        
        for (Class<?> clazz : holder.getDeclaredClasses()) {
            
            if (!clazz.isAnnotationPresent(Config.class)) {
                SLConfigLoader.log.debug("Class {} in class {} is not annotated with @Config", clazz.getName(), holder.getName());
                continue;
            }
            
            Config config = clazz.getAnnotation(Config.class);
            ModConfig.Type type = config.value();
            String name = SLConfigLoader.getConfigName(clazz, modId, config, types);
            
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            IdentityHashMap<Field, ModConfigSpec.ConfigValue<?>> values = new IdentityHashMap<>();
            SLConfigLoader.build(modId, clazz, builder, values);
            ModConfigSpec spec = builder.build();
            if (spec.isEmpty()) SLConfigLoader.throwOrLog("ModConfigSpec '" + name + "' is empty!");
            
            Cache cache = new Cache(spec, values);
            SLConfigLoader.CACHE.computeIfAbsent(modId, _ -> new ConcurrentHashMap<>()).put(spec, cache);
            
            container.registerConfig(type, spec, modId + "/" + name + ".toml");
        }
    }
    
    ///
    /// Builds a single config file
    ///
    /// @param mod     The ModId of the mod this config is being constructed for.
    /// @param clazz   Class to build the config from.
    /// @param builder Builder to build the config with.
    /// @param cache   Cache to store the config in.
    ///
    private static void build(String mod, Class<?> clazz, ModConfigSpec.Builder builder, IdentityHashMap<Field, ModConfigSpec.ConfigValue<?>> cache) {
        SLConfigLoader.log.debug("Building config from class '{}'", clazz.getCanonicalName());
        
        for (Class<?> inner : clazz.getDeclaredClasses()) {
            if (!inner.isAnnotationPresent(Group.class)) {
                SLConfigLoader.log.debug("Class {} in class {} is not annotated with @Group", inner.getName(), clazz.getName());
                continue;
            }
            
            Group group = inner.getAnnotation(Group.class);
            String name = SLConfigLoader.getGroupName(inner, group);
            
            builder.push(name);
            SLConfigLoader.build(mod, inner, builder, cache);
            builder.pop();
        }
        
        for (Field field : clazz.getDeclaredFields()) {
            final Annotation[] annotations = field.getAnnotations();
            final Optional<ConfigEntry> optional = Arrays.stream(annotations).map(_ -> field.getAnnotation(ConfigEntry.class)).filter(Objects::nonNull).findFirst();
            if (optional.isEmpty()) {
                SLConfigLoader.log.warn("Field {} in class {} is not annotated with @ConfigEntry", field.getName(), clazz.getName());
                continue;
            }
            
            final ConfigEntry entry = optional.get();
            final String key = entry.value();
            final String name = key.isBlank() ? SLStringUtils.toSnakeCase(field.getName()) : key;
            final List<String> comments = entry.comment().lines().map(String::strip).toList();
            
            try {
                if (Modifier.isFinal(field.getModifiers())) throw new IllegalStateException("Config field " + name + " must not be final");
                if (!Modifier.isStatic(field.getModifiers())) throw new IllegalStateException("Config field " + name + " must be static");
                
                field.setAccessible(true);
                Object object = field.get(null);
                ModConfigSpec.ConfigValue<?> spec = ConfigHandlers.create(mod, builder, name, field, object, comments).orElseThrow(() -> new IllegalArgumentException("Unsupported config field type " + field.getType()));
                cache.put(field, spec);
            } catch (IllegalArgumentException e) {
                SLConfigLoader.throwOrLog("Failed to build config field " + name, e);
            } catch (IllegalAccessException e) {
                SLConfigLoader.throwOrLog("Failed to access field " + name, e);
            } catch (NullPointerException e) {
                SLConfigLoader.throwOrLog("Config field " + name + " is null", e);
            }
        }
    }
    
    ///
    /// Loads the {@link ModFileScanData} from the given {@link FMLModContainer} using reflection.
    ///
    /// @param container Mod container to load the scan results from.
    ///
    /// @return Scan results.
    ///
    /// @throws RuntimeException If the scan results cannot be loaded, and we are running in the IDE.
    ///
    private static @Nullable ModFileScanData getScanResults(FMLModContainer container) {
        try {
            return (ModFileScanData) SLConfigLoader.SCAN_RESULTS.get(container);
        } catch (Throwable e) {
            SLConfigLoader.throwOrLog("Failed to get scan results for " + container.getModId(), e);
            return null;
        }
    }
    
    ///
    /// Loads the {@link Module} from the given {@link FMLModContainer} using reflection.
    ///
    /// @param container Mod container to load the module from.
    ///
    /// @return Module.
    ///
    /// @throws RuntimeException If the module cannot be loaded, and we are running in the IDE.
    ///
    private static @Nullable Module getLayer(FMLModContainer container) {
        try {
            return (Module) SLConfigLoader.LAYER.get(container);
        } catch (Throwable e) {
            SLConfigLoader.throwOrLog("Failed to get module for " + container.getModId(), e);
            return null;
        }
    }
    
    ///
    /// Throws if we are inside the IDE and otherwise logs an error.
    ///
    /// @param msg Message to log.
    /// @param e   Exception to log.
    ///
    private static void throwOrLog(String msg, Throwable e) {
        if (SharedConstants.IS_RUNNING_IN_IDE) throw new RuntimeException(msg, e);
        else SLConfigLoader.log.error(msg, e);
    }
    
    ///
    /// Throws if we are inside the IDE and otherwise logs an error.
    ///
    /// @param msg Message to log.
    ///
    private static void throwOrLog(String msg) {
        if (SharedConstants.IS_RUNNING_IN_IDE) throw new RuntimeException(msg);
        else SLConfigLoader.log.warn(msg);
    }
    
    ///
    /// Gets the name of the config file using the following pattern.
    /// <p> 1. Use the name specified in {@link Config#name()}.
    /// <p> 2. ModId plus the {@link ModConfig.Type} of the config.
    /// <p> 3. The {@link Class#getSimpleName()} of the config class.
    ///
    /// @param clazz  The config class.
    /// @param mod    The mod id.
    /// @param config The config annotation.
    /// @param types  The existing types of configs.
    ///
    /// @return The name of the config file.
    ///
    private static String getConfigName(Class<?> clazz, String mod, Config config, Set<ModConfig.Type> types) {
        final ModConfig.Type type = config.value();
        final String name = config.name();
        if (!name.isBlank()) return name;
        if (types.add(type)) return mod + "-" + type.extension();
        return SLStringUtils.toSnakeCase(clazz.getSimpleName());
    }
    
    ///
    /// Gets the group name using the following pattern.
    /// 1. Use the name specified in {@link Group#value()}.
    /// 2. The {@link Class#getSimpleName()} of the group class.
    ///
    /// @param clazz The group class.
    /// @param group The group annotation.
    ///
    /// @return The name of the group.
    ///
    private static String getGroupName(Class<?> clazz, Group group) {
        String name = group.value();
        if (!name.isBlank()) return name;
        return SLStringUtils.toSnakeCase(clazz.getSimpleName());
    }
    
    ///
    /// Translates the given mod container's config entries into the given consumer.
    ///
    /// @param container The mod container to translate the config entries for.
    /// @param consumer  The BiConsumer of the language provider
    ///
    public static void translate(ModContainer container, BiConsumer<String, String> consumer) {
        Collection<Cache> caches = SLConfigLoader.CACHE.getOrDefault(container.getModId(), new HashMap<>()).values();
        if (caches.isEmpty()) return;
        
        caches.stream().map(Cache::values).map(Map::entrySet).flatMap(Collection::stream).forEach(entry -> {
            final Field field = entry.getKey();
            final ModConfigSpec.ConfigValue<?> value = entry.getValue();
            final Annotation[] annotations = field.getAnnotations();
            SLConfigLoader.addEntryTranslation(field, value, annotations, consumer);
        });
    }
    
    ///
    /// Adds the config field translation to the given consumer.
    ///
    /// @param field       The config field.
    /// @param value       The config value.
    /// @param annotations The annotations on the config field.
    /// @param consumer    The consumer to add the translation to.
    ///
    private static void addEntryTranslation(Field field, ModConfigSpec.ConfigValue<?> value, Annotation[] annotations, BiConsumer<String, String> consumer) {
        final Optional<ConfigEntry> optional = Arrays.stream(annotations).map(_ -> field.getAnnotation(ConfigEntry.class)).filter(Objects::nonNull).findFirst();
        if (optional.isEmpty()) throw new IllegalStateException("Cache fields must always have a ConfigEntry annotation");
        
        final ConfigEntry entry = optional.get();
        final String translationKey = value.getSpec().getTranslationKey();
        if (translationKey == null) throw new IllegalStateException("Config fields must always have a translation key");
        
        final String path = String.join(".", value.getPath());
        final String translation = entry.translation();
        final String name = translation.isBlank() ? SLStringUtils.toTitleCase(path) : translation;
        consumer.accept(translationKey, name);
        SLConfigLoader.addTooltipTranslation(translationKey, field, annotations, entry, consumer);
    }
    
    ///
    /// Adds the translation for the tooltip of the given config field to the given consumer.
    ///
    /// Follows the following format:
    /// ```
    /// <Comment>
    /// \n
    /// <Range>
    /// \n
    /// Default: <Default value>
    /// ```
    ///
    /// @param key         The translation key of the config field.
    /// @param field       The config field.
    /// @param annotations The annotations on the config field.
    /// @param entry       The config entry annotation.
    /// @param consumer    The consumer to add the translation to.
    ///
    private static void addTooltipTranslation(String key, Field field, Annotation[] annotations, ConfigEntry entry, BiConsumer<String, String> consumer) {
        final List<String> lines = entry.comment().lines().map(String::strip).toList();
        
        try {
            StringBuilder builder = new StringBuilder();
            if (!lines.isEmpty()) {
                lines.forEach(comment -> builder
                        .append('§').append(ChatFormatting.DARK_GRAY.getChar())
                        .append(comment.strip())
                        .append('§').append(ChatFormatting.RESET.getChar())
                        .append("\n")
                );
                builder.append("\n");
            }
            
            if (field.getType().isEnum()) {
                final Enum<?>[] values = field.getType().asSubclass(Enum.class).getEnumConstants();
                for (Enum<?> e : values) {
                    builder.append('§').append(ChatFormatting.GRAY.getChar())
                            .append('§').append(ChatFormatting.BOLD.getChar())
                            .append(e.name())
                            .append('§').append(ChatFormatting.RESET.getChar())
                            .append('§').append(ChatFormatting.GRAY.getChar())
                            .append(": ")
                            .append(((ConfigEnum) e).description().strip())
                            .append('§').append(ChatFormatting.RESET.getChar())
                            .append("\n");
                }
            }
            
            builder.append('§').append(ChatFormatting.GRAY.getChar())
                    .append("Default: ")
                    .append(field.get(null))
                    .append('§').append(ChatFormatting.RESET.getChar());
            consumer.accept(key + ".tooltip", builder.toString().strip());
            
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access config field " + field.getName(), e);
        }
    }
    
    ///
    /// Cache of a mod spec and its values plus their associated fields.
    ///
    /// @param spec   The mod config spec.
    /// @param values All values of the config spec and their associated reflection fields.
    ///
    private record Cache(ModConfigSpec spec, Map<Field, ModConfigSpec.ConfigValue<?>> values) { }
}
