package dev.satherov.sathlib;

import dev.satherov.sathlib.util.SLResourceUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

///
/// Common entrypoint and shared identifiers for the SathLib mod.
///
@Mod(SathLib.MOD_ID)
public class SathLib {
    
    ///
    /// Shared logger for library-wide diagnostics.
    ///
    public static final Logger log = LoggerFactory.getLogger(SathLib.class);
    
    ///
    /// Mod identifier used for resource and registry names.
    ///
    public static final String MOD_ID = "sathlib";
    
    ///
    /// Creates the common mod entrypoint.
    ///
    /// @param bus       mod event bus
    /// @param container owning mod container
    ///
    public SathLib(final IEventBus bus, final FMLModContainer container) {
        
    }
    
    ///
    /// Creates an {@link Identifier} in the SathLib namespace.
    ///
    /// @param name resource path inside the {@value #MOD_ID} namespace
    ///
    /// @return identifier in the SathLib namespace
    ///
    public static Identifier id(final String name) {
        return SLResourceUtils.id(SathLib.MOD_ID, name);
    }
}
