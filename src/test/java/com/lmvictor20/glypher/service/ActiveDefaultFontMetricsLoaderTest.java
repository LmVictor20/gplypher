package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.ActiveGlyphChoice;
import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ActiveDefaultFontMetricsLoaderTest {
    @Test
    void parseGlyphChoicesFlattensBitmapRowsIntoChoices() {
        List<ActiveGlyphChoice> choices = ActiveDefaultFontMetricsLoader.parseGlyphChoices(new StringReader("""
            {
              "providers": [
                {
                  "type": "bitmap",
                  "file": "test:gui/menu_title.png",
                  "ascent": 24,
                  "height": 64,
                  "chars": ["ab", "c"]
                }
              ]
            }
            """), "minecraft:font/default.json");

        Assertions.assertEquals(3, choices.size());
        Assertions.assertEquals("menu_title.png", choices.getFirst().fileName());
        Assertions.assertEquals(24, choices.getFirst().packAscent());
        Assertions.assertEquals("a", choices.getFirst().glyph());
        Assertions.assertEquals("c", choices.getLast().glyph());
    }

    @Test
    void parseGlyphChoicesIgnoresNonBitmapProvidersAndKeepsFirstDuplicate() {
        List<ActiveGlyphChoice> choices = ActiveDefaultFontMetricsLoader.parseGlyphChoices(new StringReader("""
            {
              "providers": [
                {
                  "type": "space",
                  "advances": {
                    "a": 4
                  }
                },
                {
                  "type": "bitmap",
                  "file": "test:gui/first.png",
                  "ascent": 12,
                  "height": 32,
                  "chars": ["ab"]
                },
                {
                  "type": "bitmap",
                  "file": "test:gui/second.png",
                  "ascent": 18,
                  "height": 48,
                  "chars": ["a"]
                }
              ]
            }
            """), "minecraft:font/default.json");

        Assertions.assertEquals(2, choices.size());
        Assertions.assertEquals("first.png", choices.getFirst().fileName());
        Assertions.assertEquals(12, choices.getFirst().packAscent());
    }

    @Test
    void parseGlyphChoicesSafelyReturnsEmptyListForBrokenJson() {
        List<ActiveGlyphChoice> choices = ActiveDefaultFontMetricsLoader.parseGlyphChoicesSafely(
            new StringReader("{broken"),
            "minecraft:font/default.json"
        );

        Assertions.assertTrue(choices.isEmpty());
    }

    @Test
    void parsedChoiceMapsToTheSameGlyphMetricsData() {
        ActiveGlyphChoice choice = ActiveDefaultFontMetricsLoader.parseGlyphChoices(new StringReader("""
            {
              "providers": [
                {
                  "type": "bitmap",
                  "file": "test:gui/menu_title.png",
                  "ascent": 24,
                  "height": 64,
                  "chars": ["\uE120"]
                }
              ]
            }
            """), "minecraft:font/default.json").getFirst();

        Assertions.assertTrue(choice.matchesRawGlyphText(choice.glyph()));
        Assertions.assertEquals(choice.packAscent(), choice.toMetrics().packAscent());
        Assertions.assertEquals(choice.file(), choice.toMetrics().file());
    }
}
