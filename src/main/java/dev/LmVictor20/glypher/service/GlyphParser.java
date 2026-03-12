package dev.LmVictor20.glypher.service;

import java.util.Arrays;
import java.util.Optional;

public final class GlyphParser {
    private GlyphParser() {
    }

    public static Optional<String> firstCodePoint(String[] lines) {
        if (lines == null || lines.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(lines)
            .filter(line -> line != null && !line.isBlank())
            .map(line -> line.codePointAt(0))
            .map(codePoint -> new String(Character.toChars(codePoint)))
            .findFirst();
    }
}
