package com.lmvictor20.glypher.screen;

import com.lmvictor20.glypher.model.ActiveGlyphMetrics;
import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import com.lmvictor20.glypher.screen.preview.PreviewFactory;
import com.lmvictor20.glypher.service.GlypherServices;
import java.util.Optional;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class GlyphInputScreen extends Screen {
    private final Screen backScreen;
    private final GlypherServices services;

    private TextFieldWidget glyphField;
    private ButtonWidget previewButton;

    public GlyphInputScreen(Screen backScreen, GlypherServices services) {
        super(Text.translatable("screen.glypher.glyph_input"));
        this.backScreen = backScreen;
        this.services = services;
    }

    @Override
    protected void init() {
        GlypherSession session = this.services.currentSession();
        int panelLeft = this.width / 2 - 170;
        int panelTop = this.height / 2 - 102;

        this.glyphField = new TextFieldWidget(this.textRenderer, panelLeft + 20, panelTop + 54, 300, 20, Text.translatable("screen.glypher.glyph_input.glyph"));
        this.glyphField.setText(session.rawGlyphText());
        this.glyphField.setPlaceholder(Text.translatable("screen.glypher.glyph_input.glyph_hint"));
        this.glyphField.setChangedListener(value -> {
            session.setRawGlyphText(value);
            this.refreshButtons();
        });
        this.addDrawableChild(this.glyphField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> this.adjustAscent(-1))
            .dimensions(panelLeft + 84, panelTop + 112, 20, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> this.adjustAscent(1))
            .dimensions(panelLeft + 216, panelTop + 112, 20, 20)
            .build());

        this.previewButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.glyph_input.preview"), button ->
            this.client.setScreen(PreviewFactory.create(this.services, this.services.currentSession(), this))
        ).dimensions(panelLeft + 20, panelTop + 196, 148, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.glyph_input.back"), button -> this.client.setScreen(this.backScreen))
            .dimensions(panelLeft + 172, panelTop + 196, 148, 20)
            .build());

        this.refreshButtons();
        this.setInitialFocus(this.glyphField);
    }

    private void adjustAscent(int delta) {
        GlypherSession session = this.services.currentSession();
        session.setTitleAscent(session.titleAscent() + delta);
    }

    private void refreshButtons() {
        if (this.previewButton != null) {
            this.previewButton.active = !this.services.currentSession().rawGlyphText().isBlank();
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.backScreen);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GlypherUi.renderBackdrop(context, this.width, this.height);
        super.render(context, mouseX, mouseY, delta);

        GlypherSession session = this.services.currentSession();
        TitleCompositionResult composition = this.services.composeTitle(session);
        Optional<ActiveGlyphMetrics> detectedMetrics = this.services.detectSelectedGlyphMetrics(session);
        int recommendedAscent = this.services.recommendedFontAscent(session);
        int panelLeft = this.width / 2 - 170;
        int panelTop = this.height / 2 - 102;
        int panelRight = panelLeft + 340;

        GlypherUi.drawPanel(context, panelLeft, panelTop, panelRight, panelTop + 226);
        GlypherUi.drawHeader(
            context,
            this.textRenderer,
            this.width,
            panelTop + 12,
            this.title,
            Text.translatable("screen.glypher.glyph_input.subtitle")
        );

        GlypherUi.drawSectionLabel(context, this.textRenderer, panelLeft + 20, panelTop + 40, Text.translatable("screen.glypher.glyph_input.glyph"));
        GlypherUi.drawSectionLabel(context, this.textRenderer, panelLeft + 20, panelTop + 92, Text.translatable("screen.glypher.glyph_input.y_offset"));
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(Integer.toString(session.titleAscent())), this.width / 2, panelTop + 118, 0xFFF8F6F1);

        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.x_offset", session.xOffset()), panelLeft + 20, panelTop + 148, 0xFFE7E0D1);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.recommended_ascent", recommendedAscent), panelLeft + 20, panelTop + 164, 0xFFE6B566);
        if (detectedMetrics.isPresent()) {
            ActiveGlyphMetrics metrics = detectedMetrics.get();
            context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.pack_ascent", metrics.packAscent()), panelLeft + 170, panelTop + 148, 0xFFB7C7D3);
            context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.source", metrics.file()), panelLeft + 170, panelTop + 164, 0xFF8DA3B2);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.pack_ascent_missing"), panelLeft + 170, panelTop + 148, 0xFF8DA3B2);
        }

        Text status = session.rawGlyphText().isBlank()
            ? Text.translatable("screen.glypher.glyph_input.status.empty")
            : Text.translatable("screen.glypher.glyph_input.status.ready");
        if (!composition.valid()) {
            status = Text.translatable(composition.errorKey(), composition.requestedOffset());
        }

        context.drawCenteredTextWithShadow(this.textRenderer, status, this.width / 2, panelTop + 208, composition.valid() ? 0xFFB7C7D3 : 0xFFFF8D8D);
    }
}
