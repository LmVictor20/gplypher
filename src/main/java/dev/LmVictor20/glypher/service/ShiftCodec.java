package dev.LmVictor20.glypher.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ShiftCodec {
    public static final int MIN_OFFSET = -512;
    public static final int MAX_OFFSET = 512;

    private static final int[] STEPS = {1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1};

    private static final Map<Integer, String> POSITIVE = new LinkedHashMap<>();
    private static final Map<Integer, String> NEGATIVE = new LinkedHashMap<>();

    static {
        POSITIVE.put(1, "\uF800");
        POSITIVE.put(2, "\uF801");
        POSITIVE.put(4, "\uF802");
        POSITIVE.put(8, "\uF803");
        POSITIVE.put(16, "\uF804");
        POSITIVE.put(32, "\uF805");
        POSITIVE.put(64, "\uF806");
        POSITIVE.put(128, "\uF807");
        POSITIVE.put(256, "\uF808");
        POSITIVE.put(512, "\uF809");
        POSITIVE.put(1024, "\uF80A");

        NEGATIVE.put(1, "\uF80B");
        NEGATIVE.put(2, "\uF80C");
        NEGATIVE.put(4, "\uF80D");
        NEGATIVE.put(8, "\uF80E");
        NEGATIVE.put(16, "\uF80F");
        NEGATIVE.put(32, "\uF810");
        NEGATIVE.put(64, "\uF811");
        NEGATIVE.put(128, "\uF812");
        NEGATIVE.put(256, "\uF813");
        NEGATIVE.put(512, "\uF814");
        NEGATIVE.put(1024, "\uF815");
    }

    public int clampOffset(int value) {
        return Math.max(MIN_OFFSET, Math.min(MAX_OFFSET, value));
    }

    public String buildPrefix(int offset) {
        int clamped = clampOffset(offset);
        if (clamped == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        int remaining = Math.abs(clamped);
        Map<Integer, String> dict = clamped > 0 ? POSITIVE : NEGATIVE;

        for (int step : STEPS) {
            if (remaining >= step) {
                String glyph = dict.get(step);
                if (glyph != null) {
                    builder.append(glyph);
                }
                remaining -= step;
            }
            if (remaining == 0) {
                break;
            }
        }

        return builder.toString();
    }

    public String buildTitle(String glyph, int offset) {
        Objects.requireNonNull(glyph, "glyph");
        if (glyph.isEmpty()) {
            throw new IllegalArgumentException("glyph must not be empty");
        }
        return buildPrefix(offset) + glyph;
    }
}
