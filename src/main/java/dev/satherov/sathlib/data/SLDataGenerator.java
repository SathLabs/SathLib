package dev.satherov.sathlib.data;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.data.provider.SLLanguageProvider;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = SathLib.MOD_ID)
public class SLDataGenerator {
    
    @SubscribeEvent
    private static void onGatherData(GatherDataEvent.Client event) {
        
        event.createProvider(SLLanguageProvider::new);
    }
}

