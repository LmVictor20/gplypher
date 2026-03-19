package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TitleComposerTest {
    private final TitleComposer composer = new TitleComposer();

    @Test
    void composeBuildsSignedOffsetPrefixes() {
        TitleCompositionResult result = this.composer.compose(GlypherCatalog.defaultCatalog(), "glyph", 5);

        Assertions.assertTrue(result.valid());
        Assertions.assertEquals("<offset_4><offset_1>glyph", result.finalTitle());
    }

    @Test
    void composeRejectsUnrepresentableOffsets() {
        GlypherCatalog catalog = new GlypherCatalog(GlypherCatalog.defaultCatalog().yPresets(), java.util.List.of(new com.lmvictor20.glypher.model.OffsetGlyph(4, "<offset_4>")));

        TitleCompositionResult result = this.composer.compose(catalog, "glyph", 5);

        Assertions.assertFalse(result.valid());
        Assertions.assertEquals("glyph", result.finalTitle());
    }
}
