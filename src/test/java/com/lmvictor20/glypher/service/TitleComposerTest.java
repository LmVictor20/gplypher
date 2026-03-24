package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.ResourcePackProvider;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TitleComposerTest {
    private final TitleComposer composer = new TitleComposer();

    @Test
    void composeBuildsSignedOffsetPrefixes() {
        TitleCompositionResult result = this.composer.compose(GlypherCatalog.defaultCatalog(), ResourcePackProvider.PLAIN, "glyph", 5);

        Assertions.assertTrue(result.valid());
        Assertions.assertEquals("\uF802\uF800glyph", result.finalTitle());
        Assertions.assertEquals(0, result.previewXOffset());
    }

    @Test
    void composeRejectsUnrepresentableOffsets() {
        GlypherCatalog catalog = new GlypherCatalog(GlypherCatalog.defaultCatalog().yPresets(), java.util.List.of(new com.lmvictor20.glypher.model.OffsetGlyph(4, "<offset_4>")));

        TitleCompositionResult result = this.composer.compose(catalog, ResourcePackProvider.PLAIN, "glyph", 5);

        Assertions.assertFalse(result.valid());
        Assertions.assertEquals("glyph", result.finalTitle());
    }

    @Test
    void composeBuildsNexoTitleForNegativeOffsets() {
        TitleCompositionResult result = this.composer.compose(GlypherCatalog.defaultCatalog(), ResourcePackProvider.NEXO_ORAXEN, "☟", -5);

        Assertions.assertTrue(result.valid());
        Assertions.assertEquals("<shift:(-5)><glyph:☟>", result.finalTitle());
        Assertions.assertEquals("☟", result.previewText());
        Assertions.assertEquals(-5, result.previewXOffset());
    }

    @Test
    void composeBuildsNexoTitleForZeroOffset() {
        TitleCompositionResult result = this.composer.compose(GlypherCatalog.defaultCatalog(), ResourcePackProvider.NEXO_ORAXEN, "☟", 0);

        Assertions.assertTrue(result.valid());
        Assertions.assertEquals("<shift:(0)><glyph:☟>", result.finalTitle());
    }

    @Test
    void composeBuildsItemsAdderTitleForNegativeOffsets() {
        TitleCompositionResult result = this.composer.compose(GlypherCatalog.defaultCatalog(), ResourcePackProvider.ITEMS_ADDER, "☟", -5);

        Assertions.assertTrue(result.valid());
        Assertions.assertEquals(":offset_(-5)::☟:", result.finalTitle());
        Assertions.assertEquals("☟", result.previewText());
        Assertions.assertEquals(-5, result.previewXOffset());
    }

    @Test
    void composeBuildsItemsAdderTitleForZeroOffset() {
        TitleCompositionResult result = this.composer.compose(GlypherCatalog.defaultCatalog(), ResourcePackProvider.ITEMS_ADDER, "☟", 0);

        Assertions.assertTrue(result.valid());
        Assertions.assertEquals(":offset_(0)::☟:", result.finalTitle());
    }
}
