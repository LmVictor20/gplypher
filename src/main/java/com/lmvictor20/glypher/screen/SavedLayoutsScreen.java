package com.lmvictor20.glypher.screen;

import com.lmvictor20.glypher.model.SavedLayout;
import com.lmvictor20.glypher.screen.preview.PreviewFactory;
import com.lmvictor20.glypher.service.GlypherServices;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class SavedLayoutsScreen extends Screen {
    private final Screen backScreen;
    private final GlypherServices services;

    private TextFieldWidget searchField;
    private LayoutListWidget layoutListWidget;
    private ButtonWidget editButton;
    private ButtonWidget deleteButton;
    private SavedLayout selectedLayout;

    public SavedLayoutsScreen(@Nullable Screen backScreen, GlypherServices services) {
        super(Text.translatable("screen.glypher.layouts"));
        this.backScreen = backScreen;
        this.services = services;
    }

    @Override
    protected void init() {
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 62, 300, 20, Text.translatable("screen.glypher.layouts.search"));
        this.searchField.setPlaceholder(Text.translatable("screen.glypher.layouts.search"));
        this.searchField.setChangedListener(value -> this.refreshLayouts());
        this.addDrawableChild(this.searchField);

        this.layoutListWidget = new LayoutListWidget(this.client, this.width, this.height, 98, this.height - 60, 44);
        this.addDrawableChild(this.layoutListWidget);
        this.refreshLayouts();

        this.editButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.layouts.edit"), button -> this.editSelected())
            .dimensions(this.width / 2 - 150, this.height - 36, 96, 20)
            .build());
        this.deleteButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.layouts.delete"), button -> this.deleteSelected())
            .dimensions(this.width / 2 - 50, this.height - 36, 96, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.layouts.close"), button -> this.close())
            .dimensions(this.width / 2 + 50, this.height - 36, 96, 20)
            .build());

        this.refreshButtons();
    }

    private void refreshLayouts() {
        if (this.layoutListWidget == null) {
            return;
        }

        String query = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<SavedLayout> filtered = this.services.layouts().stream()
            .filter(layout -> query.isEmpty()
                || layout.id().toLowerCase(Locale.ROOT).contains(query)
                || layout.menuKind().label().getString().toLowerCase(Locale.ROOT).contains(query)
                || layout.provider().label().getString().toLowerCase(Locale.ROOT).contains(query))
            .toList();

        this.layoutListWidget.replaceEntries(filtered);

        if (this.selectedLayout != null && filtered.stream().noneMatch(layout -> layout.id().equalsIgnoreCase(this.selectedLayout.id()))) {
            this.selectedLayout = null;
        }

        this.refreshButtons();
    }

    private void refreshButtons() {
        boolean active = this.selectedLayout != null;
        if (this.editButton != null) {
            this.editButton.active = active;
            this.deleteButton.active = active;
        }
    }

    private void editSelected() {
        if (this.selectedLayout == null) {
            return;
        }

        this.services.replaceSession(this.selectedLayout.toSession());
        this.client.setScreen(PreviewFactory.create(this.services, this.services.currentSession(), new GlyphInputScreen(this, this.services)));
    }

    private void deleteSelected() {
        if (this.selectedLayout == null) {
            return;
        }

        SavedLayout layout = this.selectedLayout;
        this.client.setScreen(new ConfirmScreen(result -> {
            if (result) {
                this.services.deleteLayout(layout.id());
                if (this.client.player != null) {
                    this.client.player.sendMessage(Text.translatable("message.glypher.delete.success", layout.id()), false);
                }
                this.selectedLayout = null;
                this.client.setScreen(this);
                this.refreshLayouts();
            } else {
                this.client.setScreen(this);
            }
        }, Text.translatable("message.glypher.delete.confirm", layout.id()), Text.empty()));
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.backScreen);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GlypherUi.renderBackdrop(context, this.width, this.height);
        GlypherUi.drawPanel(context, this.width / 2 - 170, 18, this.width / 2 + 170, 92);
        GlypherUi.drawPanel(context, this.width / 2 - 194, 96, this.width / 2 + 194, this.height - 48);
        GlypherUi.drawPanel(context, this.width / 2 - 184, this.height - 42, this.width / 2 + 184, this.height - 10);
        super.render(context, mouseX, mouseY, delta);
        GlypherUi.drawHeader(
            context,
            this.textRenderer,
            this.width,
            28,
            this.title,
            Text.translatable("screen.glypher.layouts.subtitle")
        );
        if (this.layoutListWidget.children().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.layouts.none"), this.width / 2, this.height / 2, 0xAAAAAA);
        }
    }

    private final class LayoutListWidget extends AlwaysSelectedEntryListWidget<LayoutEntry> {
        private LayoutListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, bottom - top, top, itemHeight);
        }

        private void replaceEntries(List<SavedLayout> layouts) {
            this.clearEntries();
            for (SavedLayout layout : layouts) {
                this.addEntry(new LayoutEntry(layout));
            }
        }

        @Override
        public int getRowWidth() {
            return 336;
        }

        @Override
        protected int getScrollbarX() {
            return this.getRowRight() + 8;
        }
    }

    private final class LayoutEntry extends AlwaysSelectedEntryListWidget.Entry<LayoutEntry> {
        private final SavedLayout layout;

        private LayoutEntry(SavedLayout layout) {
            this.layout = layout;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            boolean selected = SavedLayoutsScreen.this.selectedLayout != null && SavedLayoutsScreen.this.selectedLayout.id().equalsIgnoreCase(this.layout.id());
            int backgroundColor = selected ? 0xAA214C58 : hovered ? 0x66304C5A : 0x44131C24;
            context.fill(x, y, x + entryWidth, y + entryHeight - 2, backgroundColor);
            context.fill(x, y, x + 4, y + entryHeight, selected ? 0xFFE6B566 : 0xFF35505F);
            context.drawTextWithShadow(SavedLayoutsScreen.this.textRenderer, Text.literal(this.layout.id()), x + 10, y + 5, 0xFFF8F6F1);
            context.drawTextWithShadow(
                SavedLayoutsScreen.this.textRenderer,
                Text.translatable("screen.glypher.layouts.meta", this.layout.menuKind().label(), this.layout.provider().label()),
                x + 10,
                y + 18,
                0xFFB7C7D3
            );
            context.drawTextWithShadow(
                SavedLayoutsScreen.this.textRenderer,
                Text.translatable(
                    "screen.glypher.layouts.details",
                    this.layout.xOffset(),
                    SavedLayoutsScreen.this.services.recommendedFontAscent(this.layout.toSession())
                ),
                x + 10,
                y + 31,
                0xFFE6B566
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            SavedLayoutsScreen.this.selectedLayout = this.layout;
            SavedLayoutsScreen.this.layoutListWidget.setSelected(this);
            SavedLayoutsScreen.this.refreshButtons();
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.layout.id());
        }
    }
}
