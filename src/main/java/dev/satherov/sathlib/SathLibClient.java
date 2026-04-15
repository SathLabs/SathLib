package dev.satherov.sathlib;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SathLib.MOD_ID, dist = Dist.CLIENT)
public class SathLibClient {
    
    public SathLibClient(final IEventBus bus, final FMLModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
