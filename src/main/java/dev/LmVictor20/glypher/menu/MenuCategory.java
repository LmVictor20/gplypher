package dev.LmVictor20.glypher.menu;

import org.bukkit.Material;

public enum MenuCategory {
    CHESTS("Сундуки", Material.CHEST),
    FURNACES("Печи", Material.FURNACE),
    WORKSTATIONS("Верстаки и станции", Material.CRAFTING_TABLE),
    MISC("Прочее", Material.BARREL);

    private final String displayName;
    private final Material icon;

    MenuCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }
}
