package dev.satherov.sathlib;

import dev.satherov.sathlib.util.SLResourceUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;

import net.minecraft.resources.Identifier;

@Mod(SathLib.MOD_ID)
public class SathLib {
    
    public static final String MOD_ID = "sathlib";
    
    public SathLib(final IEventBus bus, final FMLModContainer container) {
        
    }
    
    /// 
    /// {@link Identifier} under the `sathlib` namespace
    /// 
    public static Identifier id(final String name) {
        return SLResourceUtils.id(SathLib.MOD_ID, name);
    }
}
