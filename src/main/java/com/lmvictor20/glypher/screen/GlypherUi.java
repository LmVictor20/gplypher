package com.lmvictor20.glypher.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class GlypherUi {
    private GlypherUi() {
    }

    public static void renderBackdrop(DrawContext context, int width, int height) {
        context.fillGradient(0, 0, width, height, 0xFF101A22, 0xFF162330);
        context.fillGradient(0, 0, width, height, 0x2019A783, 0x10203A4C);

        int gridColor = 0x142C3944;
        for (int x = 0; x < width; x += 24) {
            context.fill(x, 0, x + 1, height, gridColor);
        }
        for (int y = 0; y < height; y += 24) {
            context.fill(0, y, width, y + 1, gridColor);
        }

        context.fill(0, 0, width, 4, 0xAAE6B566);
        context.fill(0, height - 4, width, height, 0x88203A4C);
    }

    public static void drawPanel(DrawContext context, int left, int top, int right, int bottom) {
        context.fill(left, top, right, bottom, 0xC0121A20);
        context.fill(left + 1, top + 1, right - 1, bottom - 1, 0xB0182430);
        context.fill(left, top, right, top + 1, 0xFFE6B566);
        context.fill(left, bottom - 1, right, bottom, 0xFF264A5B);
        context.fill(left, top, left + 1, bottom, 0xCC35505F);
        context.fill(right - 1, top, right, bottom, 0xCC35505F);
    }

    public static void drawHeader(DrawContext context, TextRenderer textRenderer, int width, int y, Text title, Text subtitle) {
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, y, 0xFFF8F6F1);
        if (!subtitle.getString().isBlank()) {
            context.drawCenteredTextWithShadow(textRenderer, subtitle, width / 2, y + 14, 0xFFB7C7D3);
        }
        context.fill(width / 2 - 64, y + 28, width / 2 + 64, y + 29, 0xFFE6B566);
    }

    public static void drawSectionLabel(DrawContext context, TextRenderer textRenderer, int x, int y, Text label) {
        context.drawTextWithShadow(textRenderer, label, x, y, 0xFFE6B566);
    }
}
