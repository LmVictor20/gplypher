package com.lmvictor20.glypher.screen;

import com.lmvictor20.glypher.model.SavedLayout;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import com.lmvictor20.glypher.service.GlypherServices;
import java.util.Optional;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class SaveLayoutScreen extends Screen {
    private final Screen returnScreen;
    private final GlypherServices services;

    private TextFieldWidget idField;
    private Text validationMessage = Text.empty();

    public SaveLayoutScreen(Screen returnScreen, GlypherServices services) {
        super(Text.translatable("screen.glypher.save"));
        this.returnScreen = returnScreen;
        this.services = services;
    }

    @Override
    protected void init() {
        this.idField = new TextFieldWidget(this.textRenderer, this.width / 2 - 120, this.height / 2 - 10, 240, 20, Text.translatable("screen.glypher.save.id"));
        this.idField.setText(this.services.currentSession().layoutId());
        this.addDrawableChild(this.idField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.save.confirm"), button -> this.trySave())
            .dimensions(this.width / 2 - 100, this.height / 2 + 24, 98, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.save.cancel"), button -> this.client.setScreen(this.returnScreen))
            .dimensions(this.width / 2 + 2, this.height / 2 + 24, 98, 20)
            .build());

        this.setInitialFocus(this.idField);
    }

    private void trySave() {
        String layoutId = this.idField.getText().trim();
        if (layoutId.isBlank()) {
            this.validationMessage = Text.translatable("screen.glypher.save.error.empty");
            return;
        }

        if (this.services.currentSession().rawGlyphText().isBlank()) {
            this.validationMessage = Text.translatable("screen.glypher.save.error.glyph");
            return;
        }

        TitleCompositionResult composition = this.services.composeTitle(this.services.currentSession());
        if (!composition.valid()) {
            this.validationMessage = Text.translatable(composition.errorKey(), composition.requestedOffset());
            return;
        }

        Optional<SavedLayout> existing = this.services.findLayout(layoutId);
        String currentId = this.services.currentSession().layoutId();
        if (existing.isPresent() && !existing.get().id().equalsIgnoreCase(currentId)) {
            this.client.setScreen(new ConfirmScreen(result -> {
                if (result) {
                    this.persist(layoutId);
                } else {
                    this.client.setScreen(this);
                }
            }, Text.translatable("message.glypher.save.overwrite", layoutId), Text.empty()));
            return;
        }

        this.persist(layoutId);
    }

    private void persist(String layoutId) {
        this.services.saveLayout(layoutId, this.services.currentSession());
        if (this.client.player != null) {
            this.client.player.sendMessage(Text.translatable("message.glypher.save.success", layoutId), false);
        }
        this.client.setScreen(this.returnScreen);
    }

    @Override
    public void close() {
        this.client.setScreen(this.returnScreen);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 36, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.save.id"), this.width / 2 - 120, this.height / 2 - 22, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, this.validationMessage, this.width / 2, this.height / 2 + 52, 0xFF8888);
    }
}
