package com.lmvictor20.glypher.screen.preview;

import com.lmvictor20.glypher.model.ActiveGlyphMetrics;
import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import com.lmvictor20.glypher.screen.SaveLayoutScreen;
import com.lmvictor20.glypher.service.GlypherServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class GlypherPreviewController {
    private final GlypherServices services;
    private final GlypherSession session;
    private final Screen backScreen;

    private final List<ClickableWidget> widgets = new ArrayList<>();
    private Screen ownerScreen;

    private ButtonWidget minusOneButton;
    private ButtonWidget plusOneButton;
    private ButtonWidget minusFourButton;
    private ButtonWidget plusFourButton;
    private ButtonWidget upAscentButton;
    private ButtonWidget downAscentButton;
    private ButtonWidget saveButton;

    public GlypherPreviewController(GlypherServices services, GlypherSession session, Screen backScreen) {
        this.services = services;
        this.session = session;
        this.backScreen = backScreen;
    }

    public void init(Screen ownerScreen, Consumer<ClickableWidget> widgetConsumer) {
        this.ownerScreen = ownerScreen;
        this.widgets.clear();

        int buttonWidth = 50;
        int gap = 4;
        int rowWidth = buttonWidth * 4 + gap * 3;
        int startX = 12;
        int row1Y = 16;
        int row2Y = 40;
        if (ownerScreen instanceof GlypherPreviewHost host) {
            startX = Math.max(8, host.glypher$getTitleRenderX() - rowWidth - 18);
            row1Y = Math.max(16, host.glypher$getTitleRenderY() - 12);
            row2Y = row1Y + 24;
        }

        this.minusFourButton = this.addButton(widgetConsumer, Text.translatable("screen.glypher.preview.offset.left_4"), startX, row1Y, button -> this.adjustOffset(-4), buttonWidth);
        this.minusOneButton = this.addButton(widgetConsumer, Text.translatable("screen.glypher.preview.offset.left_1"), startX + (buttonWidth + gap), row1Y, button -> this.adjustOffset(-1), buttonWidth);
        this.plusOneButton = this.addButton(widgetConsumer, Text.translatable("screen.glypher.preview.offset.right_1"), startX + (buttonWidth + gap) * 2, row1Y, button -> this.adjustOffset(1), buttonWidth);
        this.plusFourButton = this.addButton(widgetConsumer, Text.translatable("screen.glypher.preview.offset.right_4"), startX + (buttonWidth + gap) * 3, row1Y, button -> this.adjustOffset(4), buttonWidth);

        this.downAscentButton = this.addButton(widgetConsumer, Text.translatable("screen.glypher.preview.offset.down_1"), startX, row2Y, button -> this.adjustAscent(-1), buttonWidth);
        this.upAscentButton = this.addButton(widgetConsumer, Text.translatable("screen.glypher.preview.offset.up_1"), startX + (buttonWidth + gap), row2Y, button -> this.adjustAscent(1), buttonWidth);
        this.saveButton = this.addButton(widgetConsumer, Text.translatable("screen.glypher.preview.save"), startX + (buttonWidth + gap) * 2, row2Y, button -> this.openSaveScreen(), buttonWidth * 2 + gap);

        this.refreshButtons();
    }

    private ButtonWidget addButton(Consumer<ClickableWidget> widgetConsumer, Text label, int x, int y, ButtonWidget.PressAction action, int width) {
        ButtonWidget button = ButtonWidget.builder(label, action).dimensions(x, y, width, 20).build();
        widgetConsumer.accept(button);
        this.widgets.add(button);
        return button;
    }

    public void renderOverlay(DrawContext context) {
        TitleCompositionResult composition = this.services.composeTitle(this.session);
        if (this.ownerScreen instanceof GlypherPreviewHost host) {
            int titleX = host.glypher$getTitleRenderX() + composition.previewXOffset();
            int titleY = host.glypher$getTitleRenderY() - this.session.titleAscent();
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(composition.previewText()), titleX, titleY, 0xFFFFFF, false);
        }

        Optional<ActiveGlyphMetrics> detectedMetrics = this.services.detectSelectedGlyphMetrics(this.session);
        int recommendedAscent = this.services.recommendedFontAscent(this.session);
        Text status = composition.valid()
            ? detectedMetrics
                .<Text>map(metrics -> Text.translatable(
                    "screen.glypher.preview.status.detected",
                    this.session.provider().label(),
                    this.session.menuKind().label(),
                    this.session.xOffset(),
                    this.session.titleAscent(),
                    metrics.packAscent(),
                    recommendedAscent
                ))
                .orElse(Text.translatable(
                    "screen.glypher.preview.status",
                    this.session.provider().label(),
                    this.session.menuKind().label(),
                    this.session.xOffset(),
                    this.session.titleAscent(),
                    recommendedAscent
                ))
            : Text.translatable(composition.errorKey(), composition.requestedOffset());
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, status, 8, 8, composition.valid() ? 0xFFFFFF : 0xFF8888);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ClickableWidget widget : this.widgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ClickableWidget widget : this.widgets) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.inventoryKey.matchesKey(keyCode, scanCode) || keyCode == 256) {
            client.setScreen(this.backScreen);
            return true;
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    private void adjustOffset(int delta) {
        int candidate = this.session.xOffset() + delta;
        GlypherSession previewSession = this.session.copy();
        previewSession.setXOffset(candidate);
        TitleCompositionResult result = this.services.composeTitle(previewSession);
        if (result.valid()) {
            this.session.setXOffset(candidate);
            this.refreshButtons();
        }
    }

    private void adjustAscent(int delta) {
        this.session.setTitleAscent(this.session.titleAscent() + delta);
        this.refreshButtons();
    }

    private void openSaveScreen() {
        MinecraftClient.getInstance().setScreen(new SaveLayoutScreen(this.ownerScreen, this.services));
    }

    private void refreshButtons() {
        TitleCompositionResult currentComposition = this.services.composeTitle(this.session);
        boolean hasGlyph = !this.session.rawGlyphText().isBlank();

        this.minusOneButton.active = this.canApplyOffset(-1);
        this.plusOneButton.active = this.canApplyOffset(1);
        this.minusFourButton.active = this.canApplyOffset(-4);
        this.plusFourButton.active = this.canApplyOffset(4);
        this.upAscentButton.active = true;
        this.downAscentButton.active = true;
        this.saveButton.active = hasGlyph && currentComposition.valid();
    }

    private boolean canApplyOffset(int delta) {
        GlypherSession previewSession = this.session.copy();
        previewSession.setXOffset(this.session.xOffset() + delta);
        return this.services.composeTitle(previewSession).valid();
    }
}
