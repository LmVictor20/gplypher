package com.lmvictor20.glypher.repository;

import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.MenuKind;
import com.lmvictor20.glypher.model.SavedLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GlypherRepositoriesTest {
    @TempDir
    Path tempDir;

    @Test
    void layoutsRepositoryPersistsAndReloadsLayouts() {
        GlypherLayoutsRepository repository = new GlypherLayoutsRepository(this.tempDir);
        SavedLayout layout = new SavedLayout("demo", MenuKind.CHEST_9X6, "glyph", 4, 12, "<offset_4>glyph", 1L, 2L);

        repository.saveLayout(layout);

        GlypherLayoutsRepository reloaded = new GlypherLayoutsRepository(this.tempDir);
        List<SavedLayout> layouts = reloaded.loadLayouts();

        Assertions.assertEquals(1, layouts.size());
        Assertions.assertEquals("demo", layouts.getFirst().id());
        Assertions.assertEquals(MenuKind.CHEST_9X6, layouts.getFirst().menuKind());
        Assertions.assertEquals(12, layouts.getFirst().titleAscent());
    }

    @Test
    void catalogRepositoryRecoversFromBrokenJson() throws IOException {
        Path baseDir = this.tempDir.resolve("glypher");
        Files.createDirectories(baseDir);
        Files.writeString(baseDir.resolve("catalog.json"), "{broken");

        GlypherCatalogRepository repository = new GlypherCatalogRepository(baseDir);
        GlypherCatalog catalog = repository.loadCatalog();

        Assertions.assertEquals("y0", catalog.firstPresetId());
        Assertions.assertTrue(Files.list(baseDir).anyMatch(path -> path.getFileName().toString().startsWith("catalog.json.broken-")));
    }
}