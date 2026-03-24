package com.lmvictor20.glypher.repository;

import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.MenuKind;
import com.lmvictor20.glypher.model.ResourcePackProvider;
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
        SavedLayout layout = new SavedLayout("demo", MenuKind.CHEST_9X6, ResourcePackProvider.NEXO_ORAXEN, "glyph", 4, 12, "<shift:(4)><glyph:glyph>", 1L, 2L);

        repository.saveLayout(layout);

        GlypherLayoutsRepository reloaded = new GlypherLayoutsRepository(this.tempDir);
        List<SavedLayout> layouts = reloaded.loadLayouts();

        Assertions.assertEquals(1, layouts.size());
        Assertions.assertEquals("demo", layouts.getFirst().id());
        Assertions.assertEquals(MenuKind.CHEST_9X6, layouts.getFirst().menuKind());
        Assertions.assertEquals(ResourcePackProvider.NEXO_ORAXEN, layouts.getFirst().provider());
        Assertions.assertEquals(12, layouts.getFirst().titleAscent());
    }

    @Test
    void layoutsRepositoryDefaultsMissingProviderToPlain() throws IOException {
        Path baseDir = this.tempDir.resolve("glypher");
        Files.createDirectories(baseDir);
        Files.writeString(baseDir.resolve("layouts.json"), """
            [
              {
                "id": "legacy",
                "menuKind": "CHEST_9X6",
                "rawGlyphText": "glyph",
                "xOffset": 0,
                "titleAscent": 9,
                "finalTitle": "glyph",
                "createdAt": 1,
                "updatedAt": 2
              }
            ]
            """);

        GlypherLayoutsRepository repository = new GlypherLayoutsRepository(baseDir);
        SavedLayout layout = repository.loadLayouts().getFirst();

        Assertions.assertEquals(ResourcePackProvider.PLAIN, layout.provider());

        repository.saveLayout(layout);
        Assertions.assertTrue(Files.readString(baseDir.resolve("layouts.json")).contains("\"provider\": \"PLAIN\""));
    }

    @Test
    void exportRepositoryWritesProviderLine() throws IOException {
        GlypherExportRepository repository = new GlypherExportRepository(this.tempDir);
        SavedLayout layout = new SavedLayout("demo", MenuKind.CHEST_9X6, ResourcePackProvider.ITEMS_ADDER, "☟", -5, 24, ":offset_(-5)::☟:", 1L, 2L);

        Path exportPath = repository.exportLayout(layout, 24);
        String exported = Files.readString(exportPath);

        Assertions.assertTrue(exported.contains("Provider: ItemsAdder"));
        Assertions.assertTrue(exported.contains("Title: :offset_(-5)::☟:"));
        Assertions.assertTrue(exported.contains("Ascent: 24"));
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
