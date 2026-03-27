package com.lmvictor20.glypher.screen;

import com.lmvictor20.glypher.model.ActiveGlyphChoice;
import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import com.lmvictor20.glypher.screen.preview.PreviewFactory;
import com.lmvictor20.glypher.service.GlypherServices;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class GlyphInputScreen extends Screen {
    private static final int PANEL_WIDTH = 380;
    private static final int INPUT_PREVIEW_BOX_WIDTH = 36;

    private final Screen backScreen;
    private final GlypherServices services;

    private TextFieldWidget glyphField;
    private ButtonWidget previewButton;
    private GlyphChoiceListWidget glyphListWidget;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int glyphListTop;
    private int glyphListBottom;

    public GlyphInputScreen(Screen backScreen, GlypherServices services) {
        super(Text.translatable("screen.glypher.glyph_input"));
        this.backScreen = backScreen;
        this.services = services;
    }

    @Override
    protected void init() {
        GlypherSession session = this.services.currentSession();
        this.computeLayout();

        this.glyphField = new TextFieldWidget(
            this.textRenderer,
            this.panelLeft + 20,
            this.panelTop + 54,
            PANEL_WIDTH - 40,
            20,
            Text.translatable("screen.glypher.glyph_input.glyph")
        );
        this.glyphField.setText(session.rawGlyphText());
        this.glyphField.setPlaceholder(Text.translatable("screen.glypher.glyph_input.glyph_hint"));
        this.glyphField.setChangedListener(value -> {
            session.setRawGlyphText(value);
            this.refreshButtons();
            this.refreshSelectedGlyphChoice();
        });
        this.addDrawableChild(this.glyphField);

        this.glyphListWidget = new GlyphChoiceListWidget(this.client, this.width, this.height, this.glyphListTop, this.glyphListBottom, 40);
        this.addDrawableChild(this.glyphListWidget);
        this.glyphListWidget.replaceEntries(this.services.availableGlyphChoices());

        this.previewButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.glyph_input.preview"), button ->
            this.client.setScreen(PreviewFactory.create(this.services, this.services.currentSession(), this))
        ).dimensions(this.panelLeft + 20, this.panelBottom - 28, 164, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.glyph_input.back"), button -> this.client.setScreen(this.backScreen))
            .dimensions(this.panelLeft + 196, this.panelBottom - 28, 164, 20)
            .build());

        this.refreshButtons();
        this.refreshSelectedGlyphChoice();
        this.setInitialFocus(this.glyphField);
    }

    private void computeLayout() {
        int panelHeight = Math.min(420, this.height - 20);
        this.panelLeft = this.width / 2 - PANEL_WIDTH / 2;
        this.panelTop = (this.height - panelHeight) / 2;
        this.panelRight = this.panelLeft + PANEL_WIDTH;
        this.panelBottom = this.panelTop + panelHeight;
        this.glyphListTop = this.panelTop + 112;
        this.glyphListBottom = this.panelBottom - 62;
    }

    private void refreshButtons() {
        if (this.previewButton != null) {
            this.previewButton.active = !this.services.currentSession().rawGlyphText().isBlank();
        }
    }

    private void refreshSelectedGlyphChoice() {
        if (this.glyphListWidget == null) {
            return;
        }

        GlyphChoiceEntry matchingEntry = null;
        for (GlyphChoiceEntry entry : this.glyphListWidget.children()) {
            if (entry.choice.matchesRawGlyphText(this.services.currentSession().rawGlyphText())) {
                matchingEntry = entry;
                break;
            }
        }
        this.glyphListWidget.setSelected(matchingEntry);
    }

    private void chooseGlyph(ActiveGlyphChoice choice) {
        GlypherSession session = this.services.currentSession();
        session.setRawGlyphText(choice.glyph());
        if (this.glyphField != null) {
            this.glyphField.setText(choice.glyph());
            this.glyphField.setCursorToEnd(false);
        }
        this.refreshButtons();
        this.refreshSelectedGlyphChoice();
    }

    @Override
    public void close() {
        this.client.setScreen(this.backScreen);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GlypherUi.renderBackdrop(context, this.width, this.height);
        GlypherUi.drawPanel(context, this.panelLeft, this.panelTop, this.panelRight, this.panelBottom);
        super.render(context, mouseX, mouseY, delta);

        GlypherSession session = this.services.currentSession();
        TitleCompositionResult composition = this.services.composeTitle(session);

        GlypherUi.drawHeader(
            context,
            this.textRenderer,
            this.width,
            this.panelTop + 12,
            this.title,
            Text.translatable("screen.glypher.glyph_input.subtitle")
        );

        GlypherUi.drawSectionLabel(context, this.textRenderer, this.panelLeft + 20, this.panelTop + 40, Text.translatable("screen.glypher.glyph_input.glyph"));
        this.renderGlyphFieldPreview(context, session.rawGlyphText());
        GlypherUi.drawPanel(context, this.panelLeft + 14, this.glyphListTop - 8, this.panelRight - 14, this.glyphListBottom + 6);
        GlypherUi.drawSectionLabel(context, this.textRenderer, this.panelLeft + 20, this.panelTop + 92, Text.translatable("screen.glypher.glyph_input.picker"));

        if (this.glyphListWidget.children().isEmpty()) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("screen.glypher.glyph_input.picker_empty"),
                this.width / 2,
                this.glyphListTop + 18,
                0xFF8DA3B2
            );
        }

        Text status = session.rawGlyphText().isBlank()
            ? Text.translatable("screen.glypher.glyph_input.status.empty")
            : Text.translatable("screen.glypher.glyph_input.status.ready");
        if (!composition.valid()) {
            status = Text.translatable(composition.errorKey(), composition.requestedOffset());
        }

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            status,
            this.width / 2,
            this.panelBottom - 46,
            composition.valid() ? 0xFFB7C7D3 : 0xFFFF8D8D
        );
    }

    private void renderGlyphFieldPreview(DrawContext context, String rawGlyphText) {
        if (this.glyphField == null || rawGlyphText == null || rawGlyphText.isBlank()) {
            return;
        }

        String previewGlyph = firstVisibleGlyph(rawGlyphText);
        if (previewGlyph.isBlank()) {
            return;
        }

        int boxX = this.glyphField.getX() + this.glyphField.getWidth() - INPUT_PREVIEW_BOX_WIDTH - 3;
        int boxY = this.glyphField.getY() + 2;
        int boxHeight = this.glyphField.getHeight() - 4;

        context.fill(boxX, boxY, boxX + INPUT_PREVIEW_BOX_WIDTH, boxY + boxHeight, 0xAA132129);
        context.drawBorder(boxX, boxY, INPUT_PREVIEW_BOX_WIDTH, boxHeight, 0xFF35505F);
        context.enableScissor(boxX + 1, boxY + 1, boxX + INPUT_PREVIEW_BOX_WIDTH - 1, boxY + boxHeight - 1);
        context.getMatrices().push();
        context.getMatrices().translate(boxX + 5, boxY + 4, 0);
        context.getMatrices().scale(0.45F, 0.45F, 1.0F);
        context.drawTextWithShadow(this.textRenderer, previewGlyph, 0, 0, 0xFFE6B566);
        context.getMatrices().pop();
        context.disableScissor();
    }

    private static String firstVisibleGlyph(String rawGlyphText) {
        int codePoint = rawGlyphText.codePoints()
            .filter(code -> !Character.isWhitespace(code))
            .findFirst()
            .orElse(-1);
        return codePoint < 0 ? "" : new String(Character.toChars(codePoint));
    }

    private String fitToWidth(String value, int maxWidth) {
        if (this.textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }

        String ellipsis = "...";
        int ellipsisWidth = this.textRenderer.getWidth(ellipsis);
        StringBuilder builder = new StringBuilder();
        int width = 0;
        int[] codePoints = value.codePoints().toArray();
        for (int codePoint : codePoints) {
            String next = new String(Character.toChars(codePoint));
            int nextWidth = this.textRenderer.getWidth(next);
            if (width + nextWidth + ellipsisWidth > maxWidth) {
                break;
            }
            builder.append(next);
            width += nextWidth;
        }
        return builder.isEmpty() ? ellipsis : builder + ellipsis;
    }

    private final class GlyphChoiceListWidget extends AlwaysSelectedEntryListWidget<GlyphChoiceEntry> {
        private GlyphChoiceListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, bottom - top, top, itemHeight);
        }

        private void replaceEntries(List<ActiveGlyphChoice> choices) {
            this.clearEntries();
            for (ActiveGlyphChoice choice : choices) {
                this.addEntry(new GlyphChoiceEntry(choice));
            }
        }

        @Override
        public int getRowWidth() {
            return PANEL_WIDTH - 52;
        }

        @Override
        public int getRowLeft() {
            return GlyphInputScreen.this.panelLeft + 22;
        }

        @Override
        protected int getScrollbarX() {
            return GlyphInputScreen.this.panelRight - 22;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.enableScissor(
                GlyphInputScreen.this.panelLeft + 16,
                GlyphInputScreen.this.glyphListTop - 2,
                GlyphInputScreen.this.panelRight - 16,
                GlyphInputScreen.this.glyphListBottom + 2
            );
            super.renderWidget(context, mouseX, mouseY, delta);
            context.disableScissor();
        }
    }

    private final class GlyphChoiceEntry extends AlwaysSelectedEntryListWidget.Entry<GlyphChoiceEntry> {
        private final ActiveGlyphChoice choice;

        private GlyphChoiceEntry(ActiveGlyphChoice choice) {
            this.choice = choice;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            boolean selected = GlyphInputScreen.this.glyphListWidget.getSelectedOrNull() == this;
            int backgroundColor = selected ? 0xAA214C58 : hovered ? 0x66304C5A : 0x44131C24;
            context.fill(x, y, x + entryWidth, y + entryHeight, backgroundColor);
            context.fill(x, y, x + 4, y + entryHeight, selected ? 0xFFE6B566 : 0xFF35505F);

            int thumbX = x + 8;
            int thumbY = y + 6;
            int thumbWidth = 46;
            int thumbHeight = entryHeight - 12;
            context.fill(thumbX, thumbY, thumbX + thumbWidth, thumbY + thumbHeight, 0xAA0D141A);
            context.drawBorder(thumbX, thumbY, thumbWidth, thumbHeight, selected ? 0xFFE6B566 : 0xFF35505F);

            context.enableScissor(thumbX + 1, thumbY + 1, thumbX + thumbWidth - 1, thumbY + thumbHeight - 1);
            context.getMatrices().push();
            context.getMatrices().translate(thumbX + 4, thumbY + 4, 0);
            context.getMatrices().scale(0.35F, 0.35F, 1.0F);
            context.drawTextWithShadow(GlyphInputScreen.this.textRenderer, this.choice.glyph(), 0, 0, 0xFFF8F6F1);
            context.getMatrices().pop();
            context.disableScissor();

            int textX = thumbX + thumbWidth + 10;
            int glyphBoxWidth = 36;
            int textWidth = entryWidth - (textX - x) - glyphBoxWidth - 12;

            String fileLabel = GlyphInputScreen.this.fitToWidth(this.choice.fileName(), textWidth);
            String sourceLabel = GlyphInputScreen.this.fitToWidth(this.choice.file(), textWidth);
            context.drawTextWithShadow(GlyphInputScreen.this.textRenderer, fileLabel, textX, y + 7, 0xFFF8F6F1);
            context.drawTextWithShadow(GlyphInputScreen.this.textRenderer, sourceLabel, textX, y + 21, 0xFF8DA3B2);

            int glyphBoxX = x + entryWidth - glyphBoxWidth - 8;
            int glyphBoxY = y + 8;
            context.fill(glyphBoxX, glyphBoxY, glyphBoxX + glyphBoxWidth, glyphBoxY + entryHeight - 16, 0xAA132129);
            context.drawBorder(glyphBoxX, glyphBoxY, glyphBoxWidth, entryHeight - 16, 0xFF35505F);
            context.enableScissor(glyphBoxX + 1, glyphBoxY + 1, glyphBoxX + glyphBoxWidth - 1, glyphBoxY + entryHeight - 17);
            context.drawCenteredTextWithShadow(
                GlyphInputScreen.this.textRenderer,
                Text.literal(this.choice.glyph()),
                glyphBoxX + glyphBoxWidth / 2,
                y + 16,
                0xFFE6B566
            );
            context.disableScissor();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            GlyphInputScreen.this.glyphListWidget.setSelected(this);
            GlyphInputScreen.this.chooseGlyph(this.choice);
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.choice.fileName());
        }
    }

}
