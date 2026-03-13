package dev.LmVictor20.glypher.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GlyphParserTest {
    @Test
    void returnsFirstCodePointForNonEmptyInput() {
        assertEquals("\uE123", GlyphParser.firstCodePoint("\uE123text").orElseThrow());
    }

    @Test
    void handlesSurrogatePairs() {
        String rocket = new String(Character.toChars(0x1F680));
        assertEquals(rocket, GlyphParser.firstCodePoint(rocket + " test").orElseThrow());
    }

    @Test
    void emptyInputReturnsEmptyOptional() {
        assertTrue(GlyphParser.firstCodePoint(" ").isEmpty());
        assertTrue(GlyphParser.firstCodePoint(null).isEmpty());
    }
}