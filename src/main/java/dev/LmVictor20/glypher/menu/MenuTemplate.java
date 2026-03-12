package dev.LmVictor20.glypher.menu;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

public enum MenuTemplate {
    CHEST_3X9(MenuCategory.CHESTS, "Сундук 27 (3x9)", Material.CHEST, null, 27),
    CHEST_6X9(MenuCategory.CHESTS, "Сундук 54 (6x9)", Material.TRAPPED_CHEST, null, 54),

    FURNACE(MenuCategory.FURNACES, "Печь", Material.FURNACE, InventoryType.FURNACE, -1),
    BLAST_FURNACE(MenuCategory.FURNACES, "Плавильня", Material.BLAST_FURNACE, InventoryType.BLAST_FURNACE, -1),
    SMOKER(MenuCategory.FURNACES, "Коптильня", Material.SMOKER, InventoryType.SMOKER, -1),

    WORKBENCH(MenuCategory.WORKSTATIONS, "Верстак 3x3", Material.CRAFTING_TABLE, InventoryType.WORKBENCH, -1),
    CRAFTER(MenuCategory.WORKSTATIONS, "Крафтер", Material.CRAFTER, InventoryType.CRAFTER, -1),
    ANVIL(MenuCategory.WORKSTATIONS, "Наковальня", Material.ANVIL, InventoryType.ANVIL, -1),
    SMITHING(MenuCategory.WORKSTATIONS, "Кузница", Material.SMITHING_TABLE, InventoryType.SMITHING, -1),
    BREWING(MenuCategory.WORKSTATIONS, "Варочная стойка", Material.BREWING_STAND, InventoryType.BREWING, -1),
    ENCHANTING(MenuCategory.WORKSTATIONS, "Стол зачарований", Material.ENCHANTING_TABLE, InventoryType.ENCHANTING, -1),
    CARTOGRAPHY(MenuCategory.WORKSTATIONS, "Картографический стол", Material.CARTOGRAPHY_TABLE, InventoryType.CARTOGRAPHY, -1),
    GRINDSTONE(MenuCategory.WORKSTATIONS, "Точило", Material.GRINDSTONE, InventoryType.GRINDSTONE, -1),
    LOOM(MenuCategory.WORKSTATIONS, "Ткацкий станок", Material.LOOM, InventoryType.LOOM, -1),
    STONECUTTER(MenuCategory.WORKSTATIONS, "Камнерез", Material.STONECUTTER, InventoryType.STONECUTTER, -1),

    BARREL(MenuCategory.MISC, "Бочка (27)", Material.BARREL, InventoryType.BARREL, -1),
    SHULKER_BOX(MenuCategory.MISC, "Шалкер (27)", Material.SHULKER_BOX, InventoryType.SHULKER_BOX, -1),
    ENDER_CHEST(MenuCategory.MISC, "Эндер-сундук (27)", Material.ENDER_CHEST, InventoryType.ENDER_CHEST, -1),
    HOPPER(MenuCategory.MISC, "Воронка (5)", Material.HOPPER, InventoryType.HOPPER, -1),
    DROPPER(MenuCategory.MISC, "Дроппер (9)", Material.DROPPER, InventoryType.DROPPER, -1),
    DISPENSER(MenuCategory.MISC, "Раздатчик (9)", Material.DISPENSER, InventoryType.DISPENSER, -1),
    BEACON(MenuCategory.MISC, "Маяк", Material.BEACON, InventoryType.BEACON, -1),
    LECTERN(MenuCategory.MISC, "Кафедра", Material.LECTERN, InventoryType.LECTERN, -1),
    CHISELED_BOOKSHELF(MenuCategory.MISC, "Резная книжная полка", Material.CHISELED_BOOKSHELF,
        InventoryType.CHISELED_BOOKSHELF, -1);

    private final MenuCategory category;
    private final String displayName;
    private final Material icon;
    private final InventoryType inventoryType;
    private final int chestSize;

    MenuTemplate(MenuCategory category, String displayName, Material icon, InventoryType inventoryType, int chestSize) {
        this.category = category;
        this.displayName = displayName;
        this.icon = icon;
        this.inventoryType = inventoryType;
        this.chestSize = chestSize;
    }

    public MenuCategory category() {
        return category;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }

    public InventoryType inventoryType() {
        return inventoryType;
    }

    public int chestSize() {
        return chestSize;
    }

    public boolean isChestSizeTemplate() {
        return inventoryType == null;
    }

    public static List<MenuTemplate> byCategory(MenuCategory category) {
        return Arrays.stream(values())
            .filter(template -> template.category == category)
            .toList();
    }
}
