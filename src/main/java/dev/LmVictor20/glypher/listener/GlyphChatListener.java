package dev.LmVictor20.glypher.listener;

import dev.LmVictor20.glypher.service.MenuWizardController;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class GlyphChatListener implements Listener {
    private final Plugin plugin;
    private final MenuWizardController controller;

    public GlyphChatListener(Plugin plugin, MenuWizardController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!controller.isAwaitingGlyphChat(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        Bukkit.getScheduler().runTask(plugin,
            () -> controller.handleGlyphChatInput(event.getPlayer(), rawMessage));
    }
}