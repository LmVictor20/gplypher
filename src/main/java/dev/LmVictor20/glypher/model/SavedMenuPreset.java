package dev.LmVictor20.glypher.model;

import dev.LmVictor20.glypher.menu.MenuCategory;
import dev.LmVictor20.glypher.menu.MenuTemplate;
import java.time.Instant;

public record SavedMenuPreset(
    String id,
    MenuCategory category,
    MenuTemplate template,
    String glyph,
    int offsetX,
    String title,
    Instant createdAt,
    Instant updatedAt
) {
}
