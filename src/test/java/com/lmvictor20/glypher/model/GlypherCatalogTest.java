package com.lmvictor20.glypher.model;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GlypherCatalogTest {
    @Test
    void normalizedFallsBackToDefaultsWhenCatalogIsInvalid() {
        GlypherCatalog catalog = new GlypherCatalog(List.of(), List.of());

        GlypherCatalog normalized = catalog.normalized();

        Assertions.assertEquals("y0", normalized.firstPresetId());
        Assertions.assertFalse(normalized.offsetGlyphs().isEmpty());
    }

    @Test
    void presetIndexFindsConfiguredPreset() {
        GlypherCatalog catalog = GlypherCatalog.defaultCatalog();

        Assertions.assertEquals(2, catalog.presetIndex("y2"));
        Assertions.assertEquals(-1, catalog.presetIndex("missing"));
    }
}
