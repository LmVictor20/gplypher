package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.GlypherClient;
import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public final class GlypherPrinter {
    private final TitleComposer titleComposer;

    public GlypherPrinter(TitleComposer titleComposer) {
        this.titleComposer = titleComposer;
    }

    public void print(MinecraftClient client, GlypherCatalog catalog, GlypherSession session) {
        TitleCompositionResult composition = this.titleComposer.compose(catalog, session.rawGlyphText(), session.xOffset());
        if (!composition.valid()) {
            if (client.player != null) {
                client.player.sendMessage(Text.translatable("message.glypher.compose.invalid"), false);
            }
            return;
        }

        String id = session.layoutId().isBlank() ? "<draft>" : session.layoutId();
        String json = "{\"text\":\"" + composition.finalTitle().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";

        GlypherClient.LOGGER.info(
            "[Glypher] id={} menu={} xOffset={} ascent={}",
            id,
            session.menuKind().name(),
            session.xOffset(),
            session.titleAscent()
        );
        GlypherClient.LOGGER.info("[Glypher] title={}", composition.finalTitle());
        GlypherClient.LOGGER.info("[Glypher] json={}", json);
        GlypherClient.LOGGER.info("[Glypher] font/default.json ascent={}", session.titleAscent());

        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.translatable("message.glypher.print.summary", id, session.menuKind().name(), session.xOffset(), session.titleAscent()), false);
        client.player.sendMessage(Text.translatable("message.glypher.print.title", composition.finalTitle()), false);
        client.player.sendMessage(Text.translatable("message.glypher.print.font", session.titleAscent()), false);
        client.player.sendMessage(
            Text.translatable("message.glypher.print.json", json).fillStyle(
                Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, json))
            ).append(Text.literal(" ")).append(Text.translatable("message.glypher.print.copy")),
            false
        );
    }
}