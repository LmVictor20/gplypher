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
    private ButtonWidget printButton;
    private ButtonWidget deleteButton;
    private SavedLayout selectedLayout;

    public SavedLayoutsScreen(@Nullable Screen backScreen, GlypherServices services) {
        super(Text.translatable("screen.glypher.layouts"));
        this.backScreen = backScreen;
        this.services = services;
    }

    @Override
    protected void init() {
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 120, 28, 240, 20, Text.translatable("screen.glypher.layouts.search"));
        this.searchField.setPlaceholder(Text.translatable("screen.glypher.layouts.search"));
        this.searchField.setChangedListener(value -> this.refreshLayouts());
        this.addDrawableChild(this.searchField);

        this.layoutListWidget = new LayoutListWidget(this.client, this.width, this.height, 58, this.height - 52, 34);
        this.addDrawableChild(this.layoutListWidget);
        this.refreshLayouts();

        this.editButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.layouts.edit"), button -> this.editSelected())
            .dimensions(this.width / 2 - 150, this.height - 28, 72, 20)
            .build());
        this.printButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.layouts.print"), button -> this.printSelected())
            .dimensions(this.width / 2 - 74, this.height - 28, 72, 20)
            .build());
        this.deleteButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.layouts.delete"), button -> this.deleteSelected())
            .dimensions(this.width / 2 + 2, this.height - 28, 72, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.layouts.close"), button -> this.close())
            .dimensions(this.width / 2 + 78, this.height - 28, 72, 20)
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
                || layout.rawGlyphText().toLowerCase(Locale.ROOT).contains(query))
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
            this.printButton.active = active;
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

    private void printSelected() {
        if (this.selectedLayout == null) {
            return;
        }

        this.services.printer().print(this.client, this.services.catalog(), this.selectedLayout.toSession());
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
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
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
            return 300;
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
            int backgroundColor = selected ? 0x553B82F6 : hovered ? 0x33444444 : 0x22000000;
            context.fill(x, y, x + entryWidth, y + entryHeight - 2, backgroundColor);
            context.drawTextWithShadow(SavedLayoutsScreen.this.textRenderer, Text.literal(this.layout.id()), x + 6, y + 4, 0xFFFFFF);
            context.drawTextWithShadow(SavedLayoutsScreen.this.textRenderer, Text.translatable("screen.glypher.layouts.menu", this.layout.menuKind().label()), x + 6, y + 16, 0xFFFFFF);
            context.drawTextWithShadow(SavedLayoutsScreen.this.textRenderer, Text.translatable("screen.glypher.layouts.glyph", this.layout.rawGlyphText()), x + 6, y + 28, 0xFFFFFF);
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
