package com.lmvictor20.glypher.model;

import net.minecraft.text.Text;

public final class YPreset {
    private String id;
    private String labelKey;
    private String variantName;
    private int ascent;

    public YPreset() {
    }

    public YPreset(String id, String labelKey, String variantName, int ascent) {
        this.id = id;
        this.labelKey = labelKey;
        this.variantName = variantName;
        this.ascent = ascent;
    }

    public String id() {
        return this.id;
    }

    public String labelKey() {
        return this.labelKey;
    }

    public String variantName() {
        return this.variantName;
    }

    public int ascent() {
        return this.ascent;
    }

    public Text label() {
        return Text.translatable(this.labelKey);
    }
}

