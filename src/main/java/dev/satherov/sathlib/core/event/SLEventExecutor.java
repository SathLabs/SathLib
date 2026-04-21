package dev.satherov.sathlib.core.event;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.util.deferred.SLDeferredTasks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SathLib.MOD_ID)
public class SLEventExecutor {
    
    @SubscribeEvent
    public static void tick(final ServerTickEvent.Post event) {
        SLDeferredTasks.tick(event.getServer(), event::hasTime);
    }
    
    @SubscribeEvent
    public static void stop(final ServerStoppingEvent event) {
        SLDeferredTasks.clear(event.getServer());
    }
}
