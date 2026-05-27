package dev.satherov.sathlib.testmod;

import dev.satherov.sathlib.testmod.client.SLTestClientScreens;
import dev.satherov.sathlib.testmod.client.command.ClientCommands;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

///
/// Client-only entrypoint for the SathLib UI test mod.
///
@Mod(value = TestMod.MOD_ID, dist = Dist.CLIENT)
public final class TestModClient {
    
    public TestModClient(IEventBus bus, FMLModContainer container) {
        SLTestClientScreens.register(bus);
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, ClientCommands::register);
    }
}
