package dev.satherov.sathlib.testmod.client;

import dev.satherov.sathlib.testmod.client.screen.UITestBenchMenuScreen;
import dev.satherov.sathlib.testmod.common.TestMenus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

///
/// Client-side registration for the UI test mod screens.
///
public final class SLTestClientScreens {
    
    private SLTestClientScreens() { }
    
    public static void register(IEventBus bus) {
        bus.addListener(RegisterMenuScreensEvent.class, event -> event.register(TestMenus.UI_TEST_BENCH.get(), UITestBenchMenuScreen::new));
    }
}
