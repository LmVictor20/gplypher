package dev.LmVictor20.glypher.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GlyphParserTest {
    @Test
    void returnsFirstCodePointFromFirstNonEmptyLine() {
        String[] lines = {"", "\uE123text", "abc", ""};
        assertEquals("\uE123", GlyphParser.firstCodePoint(lines).orElseThrow());
    }

    @Test
    void handlesSurrogatePairs() {
        String rocket = new String(Character.toChars(0x1F680));
        String[] lines = {rocket + " test", "", "", ""};
        assertEquals(rocket, GlyphParser.firstCodePoint(lines).orElseThrow());
    }

    @Test
    void emptyInputReturnsEmptyOptional() {
        assertTrue(GlyphParser.firstCodePoint(new String[]{"", " ", "", ""}).isEmpty());
    }
}
