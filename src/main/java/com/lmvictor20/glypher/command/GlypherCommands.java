package com.lmvictor20.glypher.command;

import com.lmvictor20.glypher.screen.MenuSelectionScreen;
import com.lmvictor20.glypher.screen.SavedLayoutsScreen;
import com.lmvictor20.glypher.service.GlypherServices;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class GlypherCommands {
    private GlypherCommands() {
    }

    public static void register(GlypherServices services) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("glypher")
                .then(ClientCommandManager.literal("create").executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) {
                        context.getSource().sendError(Text.translatable("command.glypher.no_player"));
                        return 0;
                    }

                    services.resetSession();
                    openScreenNextTick(client, new MenuSelectionScreen(services));
                    context.getSource().sendFeedback(Text.translatable("command.glypher.open_create"));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(ClientCommandManager.literal("list").executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) {
                        context.getSource().sendError(Text.translatable("command.glypher.no_player"));
                        return 0;
                    }

                    openScreenNextTick(client, new SavedLayoutsScreen(null, services));
                    context.getSource().sendFeedback(Text.translatable("command.glypher.open_list"));
                    return Command.SINGLE_SUCCESS;
                }))
        ));
    }

    private static void openScreenNextTick(MinecraftClient client, Screen screen) {
        client.send(() -> client.setScreen(screen));
    }
}
