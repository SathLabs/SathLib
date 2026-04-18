package dev.satherov.sathlib;

import dev.satherov.sathlib.client.render.SLRenderPipelines;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;

@Mod(value = SathLib.MOD_ID, dist = Dist.CLIENT)
public class SathLibClient {
    
    public SathLibClient(final IEventBus bus, final FMLModContainer container) {
        SLRenderPipelines.register(bus);
    }
}
