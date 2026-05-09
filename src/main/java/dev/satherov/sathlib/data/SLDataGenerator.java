package dev.satherov.sathlib.data;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.data.impl.SLLanguageProvider;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

///
/// Static data-generation hook registration for SathLib.
///
@EventBusSubscriber(modid = SathLib.MOD_ID)
public class SLDataGenerator {
    
    private SLDataGenerator() { }
    
    @SubscribeEvent
    private static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(SLLanguageProvider::new);
    }
}
