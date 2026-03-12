package dev.LmVictor20.glypher.gui;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GlypherInventoryHolder implements InventoryHolder {
    private final UUID owner;
    private final WizardView view;

    public GlypherInventoryHolder(UUID owner, WizardView view) {
        this.owner = owner;
        this.view = view;
    }

    public UUID owner() {
        return owner;
    }

    public WizardView view() {
        return view;
    }

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("Glypher holder has no standalone inventory");
    }
}
