package com.lmvictor20.glypher.model;

public final class OffsetGlyph {
    private int amount;
    private String text;

    public OffsetGlyph() {
    }

    public OffsetGlyph(int amount, String text) {
        this.amount = amount;
        this.text = text;
    }

    public int amount() {
        return this.amount;
    }

    public String text() {
        return this.text;
    }
}

