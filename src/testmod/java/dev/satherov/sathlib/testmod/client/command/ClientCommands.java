package dev.satherov.sathlib.testmod.client.command;

import lombok.experimental.UtilityClass;

import com.mojang.brigadier.CommandDispatcher;

import dev.satherov.sathlib.client.screen.ColorPickerScreen;
import dev.satherov.sathlib.testmod.client.screen.ComponentShowcaseScreen;
import dev.satherov.sathlib.testmod.client.screen.WidgetGalleryScreen;

import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@UtilityClass
public class ClientCommands {
    
    public static void register(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("testscreen")
                        .executes(context -> ClientCommands.openGallery())
                        .then(Commands.literal("gallery").executes(context -> ClientCommands.openGallery()))
                        .then(Commands.literal("components").executes(context -> ClientCommands.openComponents()))
                        .then(Commands.literal("color").executes(context -> ClientCommands.openColorBench()))
        );
    }
    
    private static int openGallery() {
        Minecraft.getInstance().setScreen(new WidgetGalleryScreen());
        return 1;
    }
    
    private static int openColorBench() {
        Minecraft.getInstance().setScreen(new ColorPickerScreen(0x33A6FF));
        return 1;
    }
    
    private static int openComponents() {
        Minecraft.getInstance().setScreen(new ComponentShowcaseScreen());
        return 1;
    }
}
