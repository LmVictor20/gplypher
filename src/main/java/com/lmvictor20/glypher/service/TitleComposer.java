package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.OffsetGlyph;
import com.lmvictor20.glypher.model.ResourcePackProvider;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TitleComposer {
    public TitleCompositionResult compose(GlypherCatalog catalog, ResourcePackProvider provider, String rawGlyphText, int xOffset) {
        String glyphText = rawGlyphText == null ? "" : rawGlyphText;
        ResourcePackProvider resolvedProvider = provider == null ? ResourcePackProvider.PLAIN : provider;
        if (resolvedProvider == ResourcePackProvider.NEXO_ORAXEN) {
            return TitleCompositionResult.valid(
                "<shift:(" + xOffset + ")><glyph:" + glyphText + ">",
                glyphText,
                xOffset,
                xOffset
            );
        }

        if (resolvedProvider == ResourcePackProvider.ITEMS_ADDER) {
            return TitleCompositionResult.valid(
                ":offset_(" + xOffset + ")::" + glyphText + ":",
                glyphText,
                xOffset,
                xOffset
            );
        }

        if (xOffset == 0) {
            return TitleCompositionResult.valid(glyphText, 0);
        }

        List<OffsetGlyph> candidates = catalog.offsetGlyphs().stream()
            .filter(offset -> Integer.signum(offset.amount()) == Integer.signum(xOffset))
            .sorted(Comparator.comparingInt((OffsetGlyph offset) -> Math.abs(offset.amount())).reversed())
            .toList();

        if (candidates.isEmpty()) {
            return TitleCompositionResult.invalid(glyphText, xOffset, "screen.glypher.preview.error");
        }

        int remaining = Math.abs(xOffset);
        List<String> pieces = new ArrayList<>();
        while (remaining > 0) {
            OffsetGlyph matched = null;
            for (OffsetGlyph candidate : candidates) {
                if (Math.abs(candidate.amount()) <= remaining) {
                    matched = candidate;
                    break;
                }
            }

            if (matched == null) {
                return TitleCompositionResult.invalid(glyphText, xOffset, "screen.glypher.preview.error");
            }

            pieces.add(matched.text());
            remaining -= Math.abs(matched.amount());
        }

        StringBuilder title = new StringBuilder();
        for (String piece : pieces) {
            title.append(piece);
        }
        title.append(glyphText);
        return TitleCompositionResult.valid(title.toString(), xOffset);
    }
}
