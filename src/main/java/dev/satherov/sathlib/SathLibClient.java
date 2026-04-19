package dev.satherov.sathlib;

import dev.satherov.sathlib.client.render.SLRenderPipelines;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;

///
/// Client-only entrypoint for registering SathLib rendering systems.
///
@Mod(value = SathLib.MOD_ID, dist = Dist.CLIENT)
public class SathLibClient {
    
    ///
    /// Creates the client entrypoint and registers client-side systems.
    ///
    /// @param bus       mod event bus
    /// @param container owning mod container
    ///
    public SathLibClient(final IEventBus bus, final FMLModContainer container) {
        SLRenderPipelines.register(bus);
    }
}
