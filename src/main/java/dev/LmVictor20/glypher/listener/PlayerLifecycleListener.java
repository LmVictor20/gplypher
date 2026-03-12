package dev.LmVictor20.glypher.listener;

import dev.LmVictor20.glypher.service.MenuWizardController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLifecycleListener implements Listener {
    private final MenuWizardController controller;

    public PlayerLifecycleListener(MenuWizardController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        controller.cleanupPlayer(event.getPlayer());
    }
}
