package com.lmvictor20.glypher.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class GlypherCatalog {
    private List<YPreset> yPresets = new ArrayList<>();
    private List<OffsetGlyph> offsetGlyphs = new ArrayList<>();

    public GlypherCatalog() {
    }

    public GlypherCatalog(List<YPreset> yPresets, List<OffsetGlyph> offsetGlyphs) {
        this.yPresets = new ArrayList<>(yPresets);
        this.offsetGlyphs = new ArrayList<>(offsetGlyphs);
    }

    public List<YPreset> yPresets() {
        return this.yPresets;
    }

    public List<OffsetGlyph> offsetGlyphs() {
        return this.offsetGlyphs;
    }

    public Optional<YPreset> findPreset(String presetId) {
        return this.yPresets.stream().filter(preset -> preset.id().equals(presetId)).findFirst();
    }

    public int presetIndex(String presetId) {
        for (int index = 0; index < this.yPresets.size(); index++) {
            if (this.yPresets.get(index).id().equals(presetId)) {
                return index;
            }
        }

        return -1;
    }

    public String firstPresetId() {
        if (this.yPresets.isEmpty()) {
            return "y0";
        }

        return this.yPresets.get(0).id();
    }

    public GlypherCatalog normalized() {
        List<YPreset> normalizedPresets = this.yPresets.stream()
            .filter(preset -> preset.id() != null && !preset.id().isBlank() && preset.labelKey() != null && !preset.labelKey().isBlank())
            .toList();
        List<OffsetGlyph> normalizedOffsets = this.offsetGlyphs.stream()
            .filter(offset -> offset.text() != null)
            .sorted(Comparator.comparingInt((OffsetGlyph offset) -> Math.abs(offset.amount())).reversed())
            .toList();

        if (normalizedPresets.isEmpty() || normalizedOffsets.isEmpty()) {
            return defaultCatalog();
        }

        return new GlypherCatalog(normalizedPresets, normalizedOffsets);
    }

    public static GlypherCatalog defaultCatalog() {
        return new GlypherCatalog(
            List.of(
                new YPreset("y0", "ypreset.glypher.y0", "glyph_y0", 0),
                new YPreset("y1", "ypreset.glypher.y1", "glyph_y1", -2),
                new YPreset("y2", "ypreset.glypher.y2", "glyph_y2", -4),
                new YPreset("y3", "ypreset.glypher.y3", "glyph_y3", -6),
                new YPreset("y4", "ypreset.glypher.y4", "glyph_y4", -8)
            ),
            List.of(
                new OffsetGlyph(-4, "<offset_-4>"),
                new OffsetGlyph(-1, "<offset_-1>"),
                new OffsetGlyph(1, "<offset_1>"),
                new OffsetGlyph(4, "<offset_4>")
            )
        );
    }
}
