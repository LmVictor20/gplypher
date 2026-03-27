package com.lmvictor20.glypher.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lmvictor20.glypher.GlypherClient;
import com.lmvictor20.glypher.model.ActiveGlyphChoice;
import com.lmvictor20.glypher.model.ActiveGlyphMetrics;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class ActiveDefaultFontMetricsLoader {
    private ResourceManager cachedResourceManager;
    private List<ActiveGlyphChoice> cachedChoices = List.of();
    private Map<Integer, ActiveGlyphMetrics> cachedMetrics = Map.of();

    public Optional<ActiveGlyphMetrics> findMetrics(String rawGlyphText) {
        if (rawGlyphText == null || rawGlyphText.isBlank()) {
            return Optional.empty();
        }

        int codePoint = rawGlyphText.codePoints()
            .filter(code -> !Character.isWhitespace(code))
            .findFirst()
            .orElse(-1);
        if (codePoint < 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.metrics().get(codePoint));
    }

    public List<ActiveGlyphChoice> availableGlyphChoices() {
        this.ensureLoaded();
        return this.cachedChoices;
    }

    private Map<Integer, ActiveGlyphMetrics> metrics() {
        this.ensureLoaded();
        return this.cachedMetrics;
    }

    private void ensureLoaded() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getResourceManager() == null) {
            this.cachedResourceManager = null;
            this.cachedChoices = List.of();
            this.cachedMetrics = Map.of();
            return;
        }

        ResourceManager resourceManager = client.getResourceManager();
        if (resourceManager == this.cachedResourceManager) {
            return;
        }

        this.cachedResourceManager = resourceManager;
        this.cachedChoices = this.loadChoices(resourceManager);
        this.cachedMetrics = this.indexMetrics(this.cachedChoices);
    }

    private List<ActiveGlyphChoice> loadChoices(ResourceManager resourceManager) {
        try {
            Identifier fontId = Identifier.of("minecraft", "font/default.json");
            Resource resource = resourceManager.getResource(fontId).orElse(null);
            if (resource == null) {
                return List.of();
            }

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return parseGlyphChoicesSafely(reader, fontId.toString());
            }
        } catch (Exception exception) {
            GlypherClient.LOGGER.warn("Glypher could not parse the active minecraft:font/default.json for glyph data lookup.", exception);
            return List.of();
        }
    }

    static List<ActiveGlyphChoice> parseGlyphChoicesSafely(Reader reader, String fontId) {
        try {
            return parseGlyphChoices(reader, fontId);
        } catch (Exception exception) {
            return List.of();
        }
    }

    static List<ActiveGlyphChoice> parseGlyphChoices(Reader reader, String fontId) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray providers = root.getAsJsonArray("providers");
        if (providers == null) {
            return List.of();
        }

        List<ActiveGlyphChoice> choices = new ArrayList<>();
        Map<Integer, Boolean> seenCodePoints = new HashMap<>();
        for (var providerElement : providers) {
            JsonObject provider = providerElement.getAsJsonObject();
            if (!provider.has("type") || !"bitmap".equals(provider.get("type").getAsString()) || !provider.has("chars")) {
                continue;
            }

            int ascent = provider.has("ascent") ? provider.get("ascent").getAsInt() : 0;
            int height = provider.has("height") ? provider.get("height").getAsInt() : 8;
            String file = provider.has("file") ? provider.get("file").getAsString() : "<unknown>";
            String fileName = extractFileName(file);

            for (var rowElement : provider.getAsJsonArray("chars")) {
                String row = rowElement.getAsString();
                row.codePoints().forEach(codePoint -> {
                    if (seenCodePoints.putIfAbsent(codePoint, Boolean.TRUE) == null) {
                        choices.add(new ActiveGlyphChoice(codePoint, ascent, height, file, fileName, fontId));
                    }
                });
            }
        }

        return List.copyOf(choices);
    }

    private Map<Integer, ActiveGlyphMetrics> indexMetrics(List<ActiveGlyphChoice> choices) {
        if (choices.isEmpty()) {
            return Map.of();
        }

        Map<Integer, ActiveGlyphMetrics> metrics = new HashMap<>();
        for (ActiveGlyphChoice choice : choices) {
            metrics.putIfAbsent(choice.codePoint(), choice.toMetrics());
        }
        return Map.copyOf(metrics);
    }

    static String extractFileName(String file) {
        int slash = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));
        return slash >= 0 ? file.substring(slash + 1) : file;
    }
}
