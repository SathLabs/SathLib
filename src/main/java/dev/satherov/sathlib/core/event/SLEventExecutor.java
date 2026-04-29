package dev.satherov.sathlib.core.event;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.util.deferred.SLDeferredTasks;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

///
/// Event hooks that drive deferred server tasks.
///
@EventBusSubscriber(modid = SathLib.MOD_ID)
public class SLEventExecutor {
    
    private SLEventExecutor() { }
    
    ///
    /// Advances deferred tasks at the end of a server tick.
    ///
    /// @param event server tick event
    ///
    @SubscribeEvent
    public static void tick(final ServerTickEvent.Post event) {
        SLDeferredTasks.tick(event.getServer(), event::hasTime);
    }
    
    ///
    /// Clears deferred tasks when the server is stopping.
    ///
    /// @param event server stopping event
    ///
    @SubscribeEvent
    public static void stop(final ServerStoppingEvent event) {
        SLDeferredTasks.clear(event.getServer());
    }
}
