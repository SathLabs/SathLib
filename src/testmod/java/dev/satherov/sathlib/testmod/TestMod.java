package dev.satherov.sathlib.testmod;

import dev.satherov.sathlib.testmod.common.TestMenus;
import dev.satherov.sathlib.testmod.common.command.TestCommands;
import dev.satherov.sathlib.util.SLResourceUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.resources.Identifier;

///
/// Development-only test mod that exposes UI and menu benches for SathLib.
///
@Mod(TestMod.MOD_ID)
public final class TestMod {
    
    public static final String MOD_ID = "testmod";
    
    public TestMod(IEventBus bus, FMLModContainer container) {
        TestMenus.register(bus);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, TestCommands::register);
    }
    
    public static Identifier id(String path) {
        return SLResourceUtils.id(TestMod.MOD_ID, path);
    }
}
