package com.lmvictor20.glypher.screen;

import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import com.lmvictor20.glypher.screen.preview.PreviewFactory;
import com.lmvictor20.glypher.service.GlypherServices;
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

        this.glyphField = new TextFieldWidget(this.textRenderer, this.width / 2 - 120, this.height / 2 - 46, 240, 20, Text.translatable("screen.glypher.glyph_input.glyph"));
        this.glyphField.setText(session.rawGlyphText());
        this.glyphField.setPlaceholder(Text.translatable("screen.glypher.glyph_input.glyph_hint"));
        this.glyphField.setChangedListener(value -> {
            session.setRawGlyphText(value);
            this.refreshButtons();
        });
        this.addDrawableChild(this.glyphField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> this.adjustAscent(-1))
            .dimensions(this.width / 2 - 70, this.height / 2 - 6, 20, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> this.adjustAscent(1))
            .dimensions(this.width / 2 + 50, this.height / 2 - 6, 20, 20)
            .build());

        this.previewButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.glyph_input.preview"), button ->
            this.client.setScreen(PreviewFactory.create(this.services, this.services.currentSession(), this))
        ).dimensions(this.width / 2 - 100, this.height / 2 + 44, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.glyph_input.back"), button -> this.client.setScreen(this.backScreen))
            .dimensions(this.width / 2 + 2, this.height / 2 + 44, 98, 20)
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
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        GlypherSession session = this.services.currentSession();
        TitleCompositionResult composition = this.services.composeTitle(session);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 78, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.glyph"), this.width / 2 - 120, this.height / 2 - 60, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.ascent"), this.width / 2, this.height / 2 - 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(Integer.toString(session.titleAscent())), this.width / 2, this.height / 2, 0xE5E7EB);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.glyph_input.x_offset", session.xOffset()), this.width / 2, this.height / 2 + 18, 0xE5E7EB);

        Text status = session.rawGlyphText().isBlank()
            ? Text.translatable("screen.glypher.glyph_input.status.empty")
            : Text.translatable("screen.glypher.glyph_input.status.ready");
        if (!composition.valid()) {
            status = Text.translatable(composition.errorKey(), composition.requestedOffset());
        }

        context.drawCenteredTextWithShadow(this.textRenderer, status, this.width / 2, this.height / 2 + 74, 0xAAAAAA);
    }
}
