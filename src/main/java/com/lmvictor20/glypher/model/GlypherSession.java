package com.lmvictor20.glypher.model;

public final class GlypherSession {
    private MenuKind menuKind;
    private String rawGlyphText;
    private int xOffset;
    private int titleAscent;
    private String layoutId;

    public GlypherSession() {
        this(MenuKind.CHEST_9X3, "", 0, 0, "");
    }

    public GlypherSession(MenuKind menuKind, String rawGlyphText, int xOffset, int titleAscent, String layoutId) {
        this.menuKind = menuKind;
        this.rawGlyphText = rawGlyphText;
        this.xOffset = xOffset;
        this.titleAscent = titleAscent;
        this.layoutId = layoutId;
    }

    public MenuKind menuKind() {
        return this.menuKind;
    }

    public void setMenuKind(MenuKind menuKind) {
        this.menuKind = menuKind;
    }

    public String rawGlyphText() {
        return this.rawGlyphText;
    }

    public void setRawGlyphText(String rawGlyphText) {
        this.rawGlyphText = rawGlyphText;
    }

    public int xOffset() {
        return this.xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int titleAscent() {
        return this.titleAscent;
    }

    public void setTitleAscent(int titleAscent) {
        this.titleAscent = titleAscent;
    }

    public String layoutId() {
        return this.layoutId;
    }

    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
    }

    public GlypherSession copy() {
        return new GlypherSession(this.menuKind, this.rawGlyphText, this.xOffset, this.titleAscent, this.layoutId);
    }
}
