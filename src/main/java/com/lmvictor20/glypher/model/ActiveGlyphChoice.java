package com.lmvictor20.glypher.model;

public record ActiveGlyphChoice(
    int codePoint,
    int packAscent,
    int height,
    String file,
    String fileName,
    String fontId
) {
    public String glyph() {
        return new String(Character.toChars(this.codePoint));
    }

    public ActiveGlyphMetrics toMetrics() {
        return new ActiveGlyphMetrics(this.codePoint, this.packAscent, this.height, this.file, this.fontId);
    }

    public boolean matchesRawGlyphText(String rawGlyphText) {
        if (rawGlyphText == null || rawGlyphText.isBlank()) {
            return false;
        }

        int codePoint = rawGlyphText.codePoints()
            .filter(code -> !Character.isWhitespace(code))
            .findFirst()
            .orElse(-1);
        return codePoint == this.codePoint;
    }
}
