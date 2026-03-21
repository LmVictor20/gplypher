package com.lmvictor20.glypher.model;

public record ActiveGlyphMetrics(int codePoint, int packAscent, int height, String file, String fontId) {
    public String glyph() {
        return new String(Character.toChars(this.codePoint));
    }

    public int recommendedAscent(int yOffset) {
        return this.packAscent + yOffset;
    }
}
