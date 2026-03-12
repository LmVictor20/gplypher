package dev.LmVictor20.glypher.listener;

import dev.LmVictor20.glypher.gui.GlypherInventoryHolder;
import dev.LmVictor20.glypher.gui.WizardView;
import dev.LmVictor20.glypher.service.MenuWizardController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GlypherInventoryListener implements Listener {
    private final MenuWizardController controller;

    public GlypherInventoryListener(MenuWizardController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getView().getTopInventory().getHolder() instanceof GlypherInventoryHolder holder)) {
            return;
        }

        if (!holder.owner().equals(player.getUniqueId())) {
            return;
        }

        WizardView view = holder.view();
        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        boolean topClicked = rawSlot >= 0 && rawSlot < topSize;

        switch (view) {
            case CATEGORY_SELECT -> {
                event.setCancelled(true);
                if (topClicked) {
                    controller.handleCategoryClick(player, rawSlot);
                }
            }
            case TEMPLATE_SELECT -> {
                event.setCancelled(true);
                if (topClicked) {
                    controller.handleTemplateClick(player, rawSlot);
                }
            }
            case SAVED_LIST -> {
                event.setCancelled(true);
                if (topClicked) {
                    controller.handleSavedListClick(player, rawSlot, event.getClick(), event.isShiftClick());
                }
            }
            case DELETE_CONFIRM -> {
                event.setCancelled(true);
                if (topClicked) {
                    controller.handleDeleteConfirmClick(player, rawSlot);
                }
            }
            case PREVIEW_READONLY -> event.setCancelled(true);
            case PREVIEW_EDIT -> {
                event.setCancelled(true);
                if (!topClicked && event.getClickedInventory() != null
                    && event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                    controller.handlePreviewClick(player, event.getView(), event.getSlot(), event.getClick());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof GlypherInventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof GlypherInventoryHolder holder)) {
            return;
        }

        if (!holder.owner().equals(player.getUniqueId())) {
            return;
        }

        if (holder.view() == WizardView.PREVIEW_EDIT) {
            controller.onPreviewClosed(player, event.getView());
        }
    }
}
