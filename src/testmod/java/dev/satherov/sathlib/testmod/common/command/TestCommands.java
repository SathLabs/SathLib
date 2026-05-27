package dev.satherov.sathlib.testmod.common.command;

import lombok.experimental.UtilityClass;

import com.mojang.brigadier.CommandDispatcher;

import dev.satherov.sathlib.testmod.common.menu.UITestBenchMenu;

import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

@UtilityClass
public class TestCommands {
    
    private static final Component MENU_TITLE = Component.literal("SathLib UI Test Bench");
    
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("testmenu").executes(context -> TestCommands.openMenu(context.getSource().getPlayerOrException())));
    }
    
    private static int openMenu(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((containerId, inventory, ignored) -> new UITestBenchMenu(containerId, inventory), TestCommands.MENU_TITLE));
        return 1;
    }
}
