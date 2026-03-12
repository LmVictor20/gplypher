package dev.LmVictor20.glypher.service;

import dev.LmVictor20.glypher.menu.MenuCategory;
import dev.LmVictor20.glypher.menu.MenuTemplate;
import dev.LmVictor20.glypher.model.SavedMenuPreset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuPresetStorage {
    private static final String PATH_NEXT_ID = "next-id";
    private static final String PATH_SAVED = "saved-menus";

    private final JavaPlugin plugin;
    private final ShiftCodec shiftCodec;
    private final Map<String, SavedMenuPreset> presets = new LinkedHashMap<>();

    private int nextId = 1;

    public MenuPresetStorage(JavaPlugin plugin, ShiftCodec shiftCodec) {
        this.plugin = plugin;
        this.shiftCodec = shiftCodec;
    }

    public void load() {
        presets.clear();
        FileConfiguration config = plugin.getConfig();
        nextId = Math.max(1, config.getInt(PATH_NEXT_ID, 1));

        ConfigurationSection root = config.getConfigurationSection(PATH_SAVED);
        if (root == null) {
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }

            MenuCategory category = parseCategory(section.getString("category"));
            MenuTemplate template = parseTemplate(section.getString("template"));
            if (category == null || template == null) {
                plugin.getLogger().warning("Skipping invalid preset " + id + " due to unknown category/template");
                continue;
            }

            String glyph = section.getString("glyph", "");
            if (glyph.isEmpty()) {
                plugin.getLogger().warning("Skipping invalid preset " + id + " due to empty glyph");
                continue;
            }

            int offsetX = shiftCodec.clampOffset(section.getInt("offset-x", 0));
            String title = section.getString("title", shiftCodec.buildTitle(glyph, offsetX));
            Instant createdAt = parseInstant(section.getString("created-at"), Instant.now());
            Instant updatedAt = parseInstant(section.getString("updated-at"), createdAt);

            presets.put(id, new SavedMenuPreset(id, category, template, glyph, offsetX, title, createdAt, updatedAt));
        }
    }

    public List<SavedMenuPreset> listSorted() {
        return presets.values().stream()
            .sorted(Comparator.comparing(SavedMenuPreset::updatedAt).reversed())
            .toList();
    }

    public Collection<SavedMenuPreset> all() {
        return new ArrayList<>(presets.values());
    }

    public Optional<SavedMenuPreset> get(String id) {
        return Optional.ofNullable(presets.get(id));
    }

    public SavedMenuPreset saveNew(MenuCategory category, MenuTemplate template, String glyph, int offsetX) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(template, "template");

        String id = String.format("menu-%06d", nextId++);
        Instant now = Instant.now();
        int clampedOffset = shiftCodec.clampOffset(offsetX);
        String title = shiftCodec.buildTitle(glyph, clampedOffset);

        SavedMenuPreset preset = new SavedMenuPreset(id, category, template, glyph, clampedOffset, title, now, now);
        presets.put(id, preset);
        save();
        return preset;
    }

    public SavedMenuPreset upsert(String id, MenuCategory category, MenuTemplate template, String glyph, int offsetX) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(template, "template");

        SavedMenuPreset existing = presets.get(id);
        Instant now = Instant.now();
        Instant createdAt = existing != null ? existing.createdAt() : now;
        int clampedOffset = shiftCodec.clampOffset(offsetX);
        String title = shiftCodec.buildTitle(glyph, clampedOffset);

        SavedMenuPreset updated = new SavedMenuPreset(id, category, template, glyph, clampedOffset, title, createdAt, now);
        presets.put(id, updated);

        if (existing == null) {
            bumpNextIdIfNeeded(id);
        }

        save();
        return updated;
    }

    public boolean delete(String id) {
        SavedMenuPreset removed = presets.remove(id);
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    public void save() {
        FileConfiguration config = plugin.getConfig();
        config.set(PATH_NEXT_ID, nextId);
        config.set(PATH_SAVED, null);

        ConfigurationSection root = config.createSection(PATH_SAVED);
        for (SavedMenuPreset preset : presets.values()) {
            ConfigurationSection section = root.createSection(preset.id());
            section.set("category", preset.category().name());
            section.set("template", preset.template().name());
            section.set("glyph", preset.glyph());
            section.set("offset-x", preset.offsetX());
            section.set("title", preset.title());
            section.set("created-at", preset.createdAt().toString());
            section.set("updated-at", preset.updatedAt().toString());
        }

        plugin.saveConfig();
    }

    private void bumpNextIdIfNeeded(String id) {
        try {
            if (!id.startsWith("menu-")) {
                return;
            }
            int value = Integer.parseInt(id.substring("menu-".length()));
            nextId = Math.max(nextId, value + 1);
        } catch (NumberFormatException ignored) {
            // ignore malformed ids
        }
    }

    private MenuCategory parseCategory(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return MenuCategory.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private MenuTemplate parseTemplate(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return MenuTemplate.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Instant parseInstant(String raw, Instant fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Instant.parse(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
