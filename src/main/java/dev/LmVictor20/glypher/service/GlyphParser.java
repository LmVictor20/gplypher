package dev.LmVictor20.glypher.service;

import java.util.Optional;

public final class GlyphParser {
    private GlyphParser() {
    }

    public static Optional<String> firstCodePoint(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        int codePoint = input.codePointAt(0);
        return Optional.of(new String(Character.toChars(codePoint)));
    }
}