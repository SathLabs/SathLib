package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

///
/// Helper class for creating all sources of Resources
///
/// @see Identifier
/// @see ResourceKey
/// @see TagKey
///
@UtilityClass
public class SLResourceUtils {
    
    ///
    /// Shorter version of {@link Identifier#fromNamespaceAndPath(String, String)}
    ///
    /// @param namespace Mod namespace
    /// @param path      Resource path
    ///
    /// @return Resource identifier
    ///
    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
    
    ///
    /// Helper for creating a {@link Identifier} from the {@code minecraft} namespace
    ///
    /// @param path Resource path
    ///
    /// @return Resource identifier
    ///
    public static Identifier mc(String path) {
        return Identifier.withDefaultNamespace(path);
    }
    
    ///
    /// Helper for creating a {@link Identifier} from the {@code neoforge} namespace
    ///
    /// @param path Resource path
    ///
    /// @return Resource identifier
    ///
    public static Identifier neo(String path) {
        return SLResourceUtils.id("neoforge", path);
    }
    
    ///
    /// Helper for creating a {@link Identifier} from the {@code c} namespace
    ///
    /// @param path Resource path
    ///
    /// @return Resource identifier
    ///
    public static Identifier common(String path) {
        return SLResourceUtils.id("c", path);
    }
    
    ///
    /// Helper for creating a {@link ResourceKey}
    ///
    /// @param registry  Registry the key belongs to
    /// @param namespace Mod namespace
    /// @param path      Resource path
    /// @param <T>       Registry type
    ///
    /// @return Resource key
    ///
    public static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String namespace, String path) {
        return ResourceKey.create(registry, SLResourceUtils.id(namespace, path));
    }
    
    ///
    /// Helper for creating a {@link ResourceKey}
    ///
    /// @param registry  Registry the key belongs to
    /// @param namespace Mod namespace
    /// @param path      Resource path
    /// @param <T>       Registry type
    ///
    /// @return Resource key
    ///
    public static <T> ResourceKey<T> key(Registry<T> registry, String namespace, String path) {
        return ResourceKey.create(registry.key(), SLResourceUtils.id(namespace, path));
    }
    
    ///
    /// Helper for creating a {@link TagKey}
    ///
    /// @param registry  Registry the tag belongs to
    /// @param namespace Mod namespace
    /// @param path      Resource path
    /// @param <T>       Registry type
    ///
    /// @return Tag key
    ///
    public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registry, String namespace, String path) {
        return TagKey.create(registry, SLResourceUtils.id(namespace, path));
    }
    
    ///
    /// Helper for creating a {@link TagKey}
    ///
    /// @param registry  Registry the tag belongs to
    /// @param namespace Mod namespace
    /// @param path      Resource path
    /// @param <T>       Registry type
    ///
    /// @return Tag key
    ///
    public static <T> TagKey<T> tag(Registry<T> registry, String namespace, String path) {
        return TagKey.create(registry.key(), SLResourceUtils.id(namespace, path));
    }
}
