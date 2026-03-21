package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.OffsetGlyph;
import java.util.List;

public final class ShiftGlyphPalette {
    private static final List<OffsetGlyph> GLYPHS = List.of(
        new OffsetGlyph(-1024, "\uF815"),
        new OffsetGlyph(-512, "\uF814"),
        new OffsetGlyph(-256, "\uF813"),
        new OffsetGlyph(-128, "\uF812"),
        new OffsetGlyph(-64, "\uF811"),
        new OffsetGlyph(-32, "\uF810"),
        new OffsetGlyph(-16, "\uF80F"),
        new OffsetGlyph(-8, "\uF80E"),
        new OffsetGlyph(-4, "\uF80D"),
        new OffsetGlyph(-2, "\uF80C"),
        new OffsetGlyph(-1, "\uF80B"),
        new OffsetGlyph(1024, "\uF80A"),
        new OffsetGlyph(512, "\uF809"),
        new OffsetGlyph(256, "\uF808"),
        new OffsetGlyph(128, "\uF807"),
        new OffsetGlyph(64, "\uF806"),
        new OffsetGlyph(32, "\uF805"),
        new OffsetGlyph(16, "\uF804"),
        new OffsetGlyph(8, "\uF803"),
        new OffsetGlyph(4, "\uF802"),
        new OffsetGlyph(2, "\uF801"),
        new OffsetGlyph(1, "\uF800")
    );

    private ShiftGlyphPalette() {
    }

    public static List<OffsetGlyph> glyphs() {
        return GLYPHS;
    }
}
