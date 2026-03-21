package com.lmvictor20.glypher.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lmvictor20.glypher.GlypherClient;
import com.lmvictor20.glypher.model.ActiveGlyphMetrics;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class ActiveDefaultFontMetricsLoader {
    private ResourceManager cachedResourceManager;
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

    private Map<Integer, ActiveGlyphMetrics> metrics() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getResourceManager() == null) {
            return Map.of();
        }

        ResourceManager resourceManager = client.getResourceManager();
        if (resourceManager == this.cachedResourceManager && !this.cachedMetrics.isEmpty()) {
            return this.cachedMetrics;
        }

        this.cachedResourceManager = resourceManager;
        this.cachedMetrics = this.loadMetrics(resourceManager);
        return this.cachedMetrics;
    }

    private Map<Integer, ActiveGlyphMetrics> loadMetrics(ResourceManager resourceManager) {
        try {
            Identifier fontId = Identifier.of("minecraft", "font/default.json");
            Resource resource = resourceManager.getResource(fontId).orElse(null);
            if (resource == null) {
                return Map.of();
            }

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray providers = root.getAsJsonArray("providers");
                if (providers == null) {
                    return Map.of();
                }

                Map<Integer, ActiveGlyphMetrics> metrics = new HashMap<>();
                for (var providerElement : providers) {
                    JsonObject provider = providerElement.getAsJsonObject();
                    if (!provider.has("type") || !"bitmap".equals(provider.get("type").getAsString()) || !provider.has("chars")) {
                        continue;
                    }

                    int ascent = provider.has("ascent") ? provider.get("ascent").getAsInt() : 0;
                    int height = provider.has("height") ? provider.get("height").getAsInt() : 8;
                    String file = provider.has("file") ? provider.get("file").getAsString() : "<unknown>";

                    for (var rowElement : provider.getAsJsonArray("chars")) {
                        String row = rowElement.getAsString();
                        row.codePoints().forEach(codePoint ->
                            metrics.putIfAbsent(codePoint, new ActiveGlyphMetrics(codePoint, ascent, height, file, fontId.toString()))
                        );
                    }
                }

                return Map.copyOf(metrics);
            }
        } catch (Exception exception) {
            GlypherClient.LOGGER.warn("Glypher could not parse the active minecraft:font/default.json for glyph ascent lookup.", exception);
            return Map.of();
        }
    }
}
