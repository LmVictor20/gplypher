package com.lmvictor20.glypher.model;

import net.minecraft.text.Text;

public enum MenuKind {
    PLAYER_INVENTORY(MenuCategory.PLAYER, "menu.glypher.player_inventory"),
    CHEST_9X1(MenuCategory.STORAGE, "menu.glypher.chest_9x1"),
    CHEST_9X2(MenuCategory.STORAGE, "menu.glypher.chest_9x2"),
    CHEST_9X3(MenuCategory.STORAGE, "menu.glypher.chest_9x3"),
    CHEST_9X6(MenuCategory.STORAGE, "menu.glypher.chest_9x6"),
    HOPPER(MenuCategory.STORAGE, "menu.glypher.hopper"),
    DISPENSER(MenuCategory.UTILITY, "menu.glypher.dispenser"),
    DROPPER(MenuCategory.UTILITY, "menu.glypher.dropper"),
    FURNACE(MenuCategory.PROCESSING, "menu.glypher.furnace"),
    BLAST_FURNACE(MenuCategory.PROCESSING, "menu.glypher.blast_furnace"),
    SMOKER(MenuCategory.PROCESSING, "menu.glypher.smoker"),
    ANVIL(MenuCategory.UTILITY, "menu.glypher.anvil"),
    BEACON(MenuCategory.UTILITY, "menu.glypher.beacon"),
    BREWING_STAND(MenuCategory.PROCESSING, "menu.glypher.brewing_stand"),
    ENCHANTMENT(MenuCategory.UTILITY, "menu.glypher.enchantment"),
    CARTOGRAPHY(MenuCategory.CRAFTING, "menu.glypher.cartography"),
    GRINDSTONE(MenuCategory.CRAFTING, "menu.glypher.grindstone"),
    LOOM(MenuCategory.CRAFTING, "menu.glypher.loom"),
    SMITHING(MenuCategory.CRAFTING, "menu.glypher.smithing"),
    STONECUTTER(MenuCategory.CRAFTING, "menu.glypher.stonecutter"),
    CRAFTER(MenuCategory.CRAFTING, "menu.glypher.crafter");

    private final MenuCategory category;
    private final String translationKey;

    MenuKind(MenuCategory category, String translationKey) {
        this.category = category;
        this.translationKey = translationKey;
    }

    public MenuCategory category() {
        return this.category;
    }

    public Text label() {
        return Text.translatable(this.translationKey);
    }

    public String translationKey() {
        return this.translationKey;
    }
}

