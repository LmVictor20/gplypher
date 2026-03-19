package com.lmvictor20.glypher.model;

public final class TitleCompositionResult {
    private final boolean valid;
    private final String finalTitle;
    private final String errorKey;
    private final int requestedOffset;

    private TitleCompositionResult(boolean valid, String finalTitle, String errorKey, int requestedOffset) {
        this.valid = valid;
        this.finalTitle = finalTitle;
        this.errorKey = errorKey;
        this.requestedOffset = requestedOffset;
    }

    public static TitleCompositionResult valid(String finalTitle, int requestedOffset) {
        return new TitleCompositionResult(true, finalTitle, null, requestedOffset);
    }

    public static TitleCompositionResult invalid(String partialTitle, int requestedOffset, String errorKey) {
        return new TitleCompositionResult(false, partialTitle, errorKey, requestedOffset);
    }

    public boolean valid() {
        return this.valid;
    }

    public String finalTitle() {
        return this.finalTitle;
    }

    public String errorKey() {
        return this.errorKey;
    }

    public int requestedOffset() {
        return this.requestedOffset;
    }
}

