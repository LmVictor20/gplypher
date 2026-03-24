package com.lmvictor20.glypher.model;

public final class TitleCompositionResult {
    private final boolean valid;
    private final String finalTitle;
    private final String previewText;
    private final int previewXOffset;
    private final String errorKey;
    private final int requestedOffset;

    private TitleCompositionResult(boolean valid, String finalTitle, String previewText, int previewXOffset, String errorKey, int requestedOffset) {
        this.valid = valid;
        this.finalTitle = finalTitle;
        this.previewText = previewText;
        this.previewXOffset = previewXOffset;
        this.errorKey = errorKey;
        this.requestedOffset = requestedOffset;
    }

    public static TitleCompositionResult valid(String finalTitle, int requestedOffset) {
        return new TitleCompositionResult(true, finalTitle, finalTitle, 0, null, requestedOffset);
    }

    public static TitleCompositionResult invalid(String partialTitle, int requestedOffset, String errorKey) {
        return new TitleCompositionResult(false, partialTitle, partialTitle, 0, errorKey, requestedOffset);
    }

    public static TitleCompositionResult valid(String finalTitle, String previewText, int previewXOffset, int requestedOffset) {
        return new TitleCompositionResult(true, finalTitle, previewText, previewXOffset, null, requestedOffset);
    }

    public boolean valid() {
        return this.valid;
    }

    public String finalTitle() {
        return this.finalTitle;
    }

    public String previewText() {
        return this.previewText;
    }

    public int previewXOffset() {
        return this.previewXOffset;
    }

    public String errorKey() {
        return this.errorKey;
    }

    public int requestedOffset() {
        return this.requestedOffset;
    }
}
