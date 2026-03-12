package dev.LmVictor20.glypher.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShiftCodecTest {
    private final ShiftCodec codec = new ShiftCodec();

    @Test
    void buildPrefixZeroIsEmpty() {
        assertEquals("", codec.buildPrefix(0));
    }

    @Test
    void buildPrefixPositiveAndNegativeValues() {
        assertEquals("\uF800", codec.buildPrefix(1));
        assertEquals("\uF802\uF800", codec.buildPrefix(5));
        assertEquals("\uF814", codec.buildPrefix(-512));
        assertEquals("\uF80D", codec.buildPrefix(-4));
    }

    @Test
    void clampOffset() {
        assertEquals(512, codec.clampOffset(2048));
        assertEquals(-512, codec.clampOffset(-9999));
        assertEquals(17, codec.clampOffset(17));
    }

    @Test
    void buildTitleAlwaysEndsWithGlyph() {
        String title = codec.buildTitle("\uE123", -5);
        assertTrue(title.endsWith("\uE123"));
    }
}
