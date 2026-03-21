package com.lmvictor20.glypher.repository;

import com.lmvictor20.glypher.model.SavedLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class GlypherExportRepository {
    private final Path exportsDir;

    public GlypherExportRepository(Path baseDir) {
        this.exportsDir = baseDir.resolve("exports");
    }

    public Path exportLayout(SavedLayout layout, int recommendedAscent) {
        try {
            Files.createDirectories(this.exportsDir);
            Path filePath = this.exportsDir.resolve(this.sanitizeFileName(layout.id()) + ".txt");
            String content = "ID: " + layout.id() + System.lineSeparator()
                + "Title: " + layout.finalTitle() + System.lineSeparator()
                + "Ascent: " + recommendedAscent + System.lineSeparator();
            Files.writeString(
                filePath,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
            return filePath;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to export Glypher layout file", exception);
        }
    }

    public void deleteExport(String layoutId) {
        try {
            Files.deleteIfExists(this.exportsDir.resolve(this.sanitizeFileName(layoutId) + ".txt"));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to delete Glypher export file", exception);
        }
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
