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
        int panelLeft = this.width / 2 - 170;
        int panelTop = this.height / 2 - 74;

        this.idField = new TextFieldWidget(this.textRenderer, panelLeft + 20, panelTop + 56, 300, 20, Text.translatable("screen.glypher.save.id"));
        this.idField.setText(this.services.currentSession().layoutId());
        this.addDrawableChild(this.idField);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.save.confirm"), button -> this.trySave())
            .dimensions(panelLeft + 20, panelTop + 100, 148, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.save.cancel"), button -> this.client.setScreen(this.returnScreen))
            .dimensions(panelLeft + 172, panelTop + 100, 148, 20)
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
        SavedLayout layout = this.services.saveLayout(layoutId, this.services.currentSession());
        var exportPath = this.services.exportLayout(layout);
        if (this.client.player != null) {
            this.client.player.sendMessage(Text.translatable("message.glypher.save.success", layoutId), false);
            this.client.player.sendMessage(Text.translatable("message.glypher.save.exported", exportPath.toString()), false);
        }
        this.client.setScreen(this.returnScreen);
    }

    @Override
    public void close() {
        this.client.setScreen(this.returnScreen);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GlypherUi.renderBackdrop(context, this.width, this.height);
        int panelLeft = this.width / 2 - 170;
        int panelTop = this.height / 2 - 74;
        GlypherUi.drawPanel(context, panelLeft, panelTop, panelLeft + 340, panelTop + 140);
        super.render(context, mouseX, mouseY, delta);
        GlypherUi.drawHeader(
            context,
            this.textRenderer,
            this.width,
            panelTop + 14,
            this.title,
            Text.translatable("screen.glypher.save.subtitle")
        );
        GlypherUi.drawSectionLabel(context, this.textRenderer, panelLeft + 20, panelTop + 42, Text.translatable("screen.glypher.save.id"));
        context.drawCenteredTextWithShadow(this.textRenderer, this.validationMessage, this.width / 2, panelTop + 126, 0xFFFF8D8D);
    }
}
