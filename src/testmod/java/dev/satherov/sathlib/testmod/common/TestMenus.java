package dev.satherov.sathlib.testmod.common;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.testmod.TestMod;
import dev.satherov.sathlib.testmod.common.menu.UITestBenchMenu;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

@UtilityClass
public final class TestMenus {
    
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TestMod.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<UITestBenchMenu>> UI_TEST_BENCH = TestMenus.MENUS.register(
            "ui_test_bench",
            () -> new MenuType<>(UITestBenchMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
    
    public static void register(IEventBus bus) {
        TestMenus.MENUS.register(bus);
    }
}
