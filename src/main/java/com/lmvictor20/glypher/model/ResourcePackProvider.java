package com.lmvictor20.glypher.model;

import net.minecraft.text.Text;

public enum ResourcePackProvider {
    NEXO_ORAXEN("provider.glypher.nexo_oraxen", "Nexo/Oraxen"),
    ITEMS_ADDER("provider.glypher.items_adder", "ItemsAdder"),
    PLAIN("provider.glypher.plain", "Plain");

    private final String translationKey;
    private final String exportLabel;

    ResourcePackProvider(String translationKey, String exportLabel) {
        this.translationKey = translationKey;
        this.exportLabel = exportLabel;
    }

    public Text label() {
        return Text.translatable(this.translationKey);
    }

    public String exportLabel() {
        return this.exportLabel;
    }
}
