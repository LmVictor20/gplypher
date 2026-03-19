package com.lmvictor20.glypher.model;

public final class SavedLayout {
    private String id;
    private MenuKind menuKind;
    private String rawGlyphText;
    private int xOffset;
    private int titleAscent;
    private String finalTitle;
    private long createdAt;
    private long updatedAt;

    public SavedLayout() {
    }

    public SavedLayout(String id, MenuKind menuKind, String rawGlyphText, int xOffset, int titleAscent, String finalTitle, long createdAt, long updatedAt) {
        this.id = id;
        this.menuKind = menuKind;
        this.rawGlyphText = rawGlyphText;
        this.xOffset = xOffset;
        this.titleAscent = titleAscent;
        this.finalTitle = finalTitle;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String id() {
        return this.id;
    }

    public MenuKind menuKind() {
        return this.menuKind;
    }

    public String rawGlyphText() {
        return this.rawGlyphText;
    }

    public int xOffset() {
        return this.xOffset;
    }

    public int titleAscent() {
        return this.titleAscent;
    }

    public String finalTitle() {
        return this.finalTitle;
    }

    public long createdAt() {
        return this.createdAt;
    }

    public long updatedAt() {
        return this.updatedAt;
    }

    public GlypherSession toSession() {
        return new GlypherSession(this.menuKind, this.rawGlyphText, this.xOffset, this.titleAscent, this.id);
    }
}
