package dev.satherov.sathlib.compat;

import net.neoforged.fml.ModList;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/// 
/// Interface for a compatability enum
/// 
public interface CompatMod {
    
    /// 
    /// Gets the mod id of the integration
    /// 
    /// @return mod id
    /// 
    String getModId();
    
    /// 
    /// Checks if the mod is currently loaded 
    /// 
    /// @return `true` if the mod is loaded, `false` otherwise
    /// 
    default boolean isLoaded() {
        return ModList.get().isLoaded(this.getModId());
    }
    
    /// 
    /// Executes the given runnable if the mod is loaded
    /// 
    /// @param runnable runnable to execute
    /// 
    default void run(Runnable runnable) {
        if (this.isLoaded()) runnable.run();
    }
    
    /// 
    /// Gets the result of the given supplier if the mod is loaded, otherwise `null`
    /// 
    /// @param supplier supplier to get the result from
    /// 
    /// @return the result of the supplier or `null`
    /// 
    /// @param <T> type of the result
    /// 
    /// @see #run(Supplier, Object)
    /// 
    default <T> @Nullable T run(Supplier<T> supplier) {
        if (this.isLoaded()) return supplier.get();
        return null;
    }
    
    /// 
    /// Gets the result of the given supplier if the mod is loaded, otherwise the given default value
    /// 
    /// @param supplier    supplier to get the result from
    /// 
    /// @param defaultValue default value to return if the mod is not loaded
    /// 
    /// @return the result of the supplier or the default value
    /// 
    /// @param <T> type of the result
    /// 
    /// @see #run(Supplier)
    /// 
    default <T> T run(Supplier<T> supplier, T defaultValue) {
        if (this.isLoaded()) return supplier.get();
        return defaultValue;
    }
}
