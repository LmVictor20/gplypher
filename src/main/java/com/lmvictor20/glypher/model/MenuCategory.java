package com.lmvictor20.glypher.model;

import net.minecraft.text.Text;

public enum MenuCategory {
    ALL("screen.glypher.menu_selection.category.all"),
    PLAYER("screen.glypher.menu_selection.category.player"),
    STORAGE("screen.glypher.menu_selection.category.storage"),
    PROCESSING("screen.glypher.menu_selection.category.processing"),
    UTILITY("screen.glypher.menu_selection.category.utility"),
    CRAFTING("screen.glypher.menu_selection.category.crafting");

    private final String translationKey;

    MenuCategory(String translationKey) {
        this.translationKey = translationKey;
    }

    public Text label() {
        return Text.translatable(this.translationKey);
    }
}

