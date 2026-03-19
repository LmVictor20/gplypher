package com.lmvictor20.glypher.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lmvictor20.glypher.GlypherClient;
import com.lmvictor20.glypher.model.OffsetGlyph;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public final class ActiveShiftGlyphLoader {
    public List<OffsetGlyph> loadShiftGlyphs() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.getResourceManager() == null) {
                return List.of();
            }

            Identifier shiftId = Identifier.of("oraxen", "font/shift.json");
            Resource resource = client.getResourceManager().getResource(shiftId).orElse(null);
            if (resource == null) {
                return List.of();
            }

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                List<OffsetGlyph> glyphs = new ArrayList<>();
                for (var providerElement : root.getAsJsonArray("providers")) {
                    JsonObject provider = providerElement.getAsJsonObject();
                    if (!provider.has("type") || !"space".equals(provider.get("type").getAsString()) || !provider.has("advances")) {
                        continue;
                    }

                    for (var entry : provider.getAsJsonObject("advances").entrySet()) {
                        glyphs.add(new OffsetGlyph(entry.getValue().getAsInt(), entry.getKey()));
                    }
                }

                glyphs.sort(Comparator.comparingInt((OffsetGlyph glyph) -> Math.abs(glyph.amount())).reversed());
                return glyphs;
            }
        } catch (Exception exception) {
            GlypherClient.LOGGER.warn("Glypher could not load oraxen/font/shift.json from the active resource packs.", exception);
            return List.of();
        }
    }
}
