package com.lmvictor20.glypher.screen;

import com.lmvictor20.glypher.model.MenuCategory;
import com.lmvictor20.glypher.model.MenuKind;
import com.lmvictor20.glypher.model.ResourcePackProvider;
import com.lmvictor20.glypher.service.GlypherServices;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class MenuSelectionScreen extends Screen {
    private final GlypherServices services;

    private TextFieldWidget searchField;
    private MenuListWidget menuListWidget;
    private ButtonWidget nextButton;
    private final Map<ResourcePackProvider, ButtonWidget> providerButtons = new EnumMap<>(ResourcePackProvider.class);
    private MenuCategory selectedCategory = MenuCategory.ALL;
    private MenuKind selectedMenuKind;
    private ResourcePackProvider selectedProvider;

    public MenuSelectionScreen(GlypherServices services) {
        super(Text.translatable("screen.glypher.menu_selection"));
        this.services = services;
        this.selectedProvider = services.currentSession().provider();
    }

    @Override
    protected void init() {
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 58, 300, 20, Text.translatable("screen.glypher.menu_selection.search"));
        this.searchField.setPlaceholder(Text.translatable("screen.glypher.menu_selection.search"));
        this.searchField.setChangedListener(value -> this.refreshMenuList());
        this.addDrawableChild(this.searchField);

        this.initCategoryButtons();
        this.initProviderButtons();

        this.menuListWidget = new MenuListWidget(this.client, this.width, this.height, 188, this.height - 60, 34);
        this.addDrawableChild(this.menuListWidget);
        this.refreshMenuList();

        this.nextButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.menu_selection.next"), button -> {
            if (this.selectedMenuKind != null) {
                this.services.newSessionForMenu(this.selectedMenuKind, this.selectedProvider);
                this.client.setScreen(new GlyphInputScreen(this, this.services));
            }
        }).dimensions(this.width / 2 - 150, this.height - 36, 148, 20).build());
        this.nextButton.active = false;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.glypher.menu_selection.cancel"), button -> this.close())
            .dimensions(this.width / 2 + 2, this.height - 36, 148, 20)
            .build());

        this.setInitialFocus(this.searchField);
    }

    private void initCategoryButtons() {
        MenuCategory[] rowOne = {MenuCategory.ALL, MenuCategory.PLAYER, MenuCategory.STORAGE, MenuCategory.PROCESSING, MenuCategory.UTILITY};
        int buttonWidth = 72;
        int gap = 4;
        int totalWidth = rowOne.length * buttonWidth + (rowOne.length - 1) * gap;
        int startX = (this.width - totalWidth) / 2;

        for (int index = 0; index < rowOne.length; index++) {
            MenuCategory category = rowOne[index];
            int x = startX + index * (buttonWidth + gap);
            this.addDrawableChild(ButtonWidget.builder(category.label(), button -> {
                this.selectedCategory = category;
                this.refreshMenuList();
            }).dimensions(x, 86, buttonWidth, 20).build());
        }

        this.addDrawableChild(ButtonWidget.builder(MenuCategory.CRAFTING.label(), button -> {
            this.selectedCategory = MenuCategory.CRAFTING;
            this.refreshMenuList();
        }).dimensions(this.width / 2 - buttonWidth / 2, 112, buttonWidth, 20).build());
    }

    private void initProviderButtons() {
        this.providerButtons.clear();

        ResourcePackProvider[] providers = {
            ResourcePackProvider.NEXO_ORAXEN,
            ResourcePackProvider.ITEMS_ADDER,
            ResourcePackProvider.PLAIN
        };
        int buttonWidth = 92;
        int gap = 6;
        int totalWidth = providers.length * buttonWidth + (providers.length - 1) * gap;
        int startX = (this.width - totalWidth) / 2;

        for (int index = 0; index < providers.length; index++) {
            ResourcePackProvider provider = providers[index];
            int x = startX + index * (buttonWidth + gap);
            ButtonWidget button = this.addDrawableChild(ButtonWidget.builder(provider.label(), ignored -> {
                this.selectedProvider = provider;
                this.services.currentSession().setProvider(provider);
                this.refreshProviderButtons();
            }).dimensions(x, 150, buttonWidth, 20).build());
            this.providerButtons.put(provider, button);
        }

        this.refreshProviderButtons();
    }

    private void refreshProviderButtons() {
        for (Map.Entry<ResourcePackProvider, ButtonWidget> entry : this.providerButtons.entrySet()) {
            entry.getValue().active = entry.getKey() != this.selectedProvider;
        }
    }

    private void refreshMenuList() {
        if (this.menuListWidget == null) {
            return;
        }

        String query = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<MenuKind> filtered = Arrays.stream(MenuKind.values())
            .filter(menuKind -> this.selectedCategory == MenuCategory.ALL || menuKind.category() == this.selectedCategory)
            .filter(menuKind -> query.isEmpty()
                || menuKind.label().getString().toLowerCase(Locale.ROOT).contains(query)
                || menuKind.name().toLowerCase(Locale.ROOT).contains(query))
            .toList();

        this.menuListWidget.replaceEntries(filtered);

        if (this.selectedMenuKind != null && filtered.stream().noneMatch(menuKind -> menuKind == this.selectedMenuKind)) {
            this.selectedMenuKind = null;
        }

        if (this.nextButton != null) {
            this.nextButton.active = this.selectedMenuKind != null;
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GlypherUi.renderBackdrop(context, this.width, this.height);
        GlypherUi.drawPanel(context, this.width / 2 - 170, 18, this.width / 2 + 170, 180);
        GlypherUi.drawPanel(context, this.width / 2 - 184, 184, this.width / 2 + 184, this.height - 48);
        GlypherUi.drawPanel(context, this.width / 2 - 160, this.height - 42, this.width / 2 + 160, this.height - 10);
        super.render(context, mouseX, mouseY, delta);
        GlypherUi.drawHeader(
            context,
            this.textRenderer,
            this.width,
            28,
            this.title,
            Text.translatable("screen.glypher.menu_selection.subtitle")
        );
        GlypherUi.drawSectionLabel(context, this.textRenderer, this.width / 2 - 40, 138, Text.translatable("screen.glypher.menu_selection.provider"));

        if (this.menuListWidget.children().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.glypher.menu_selection.none"), this.width / 2, this.height / 2, 0xAAAAAA);
        }
    }

    private final class MenuListWidget extends AlwaysSelectedEntryListWidget<MenuEntry> {
        private MenuListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, bottom - top, top, itemHeight);
        }

        private void replaceEntries(List<MenuKind> menuKinds) {
            this.clearEntries();
            for (MenuKind menuKind : menuKinds) {
                this.addEntry(new MenuEntry(menuKind));
            }
        }

        @Override
        public int getRowWidth() {
            return 332;
        }

        @Override
        protected int getScrollbarX() {
            return this.getRowRight() + 8;
        }
    }

    private final class MenuEntry extends AlwaysSelectedEntryListWidget.Entry<MenuEntry> {
        private final MenuKind menuKind;

        private MenuEntry(MenuKind menuKind) {
            this.menuKind = menuKind;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            boolean selected = MenuSelectionScreen.this.selectedMenuKind == this.menuKind;
            int backgroundColor = selected ? 0xAA214C58 : hovered ? 0x66304C5A : 0x44131C24;
            context.fill(x, y, x + entryWidth, y + entryHeight, backgroundColor);
            context.fill(x, y, x + 4, y + entryHeight, selected ? 0xFFE6B566 : 0xFF35505F);
            context.drawTextWithShadow(MenuSelectionScreen.this.textRenderer, this.menuKind.label(), x + 10, y + 6, 0xFFF8F6F1);
            context.drawTextWithShadow(MenuSelectionScreen.this.textRenderer, this.menuKind.category().label(), x + 10, y + 21, 0xFFB7C7D3);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MenuSelectionScreen.this.selectedMenuKind = this.menuKind;
            MenuSelectionScreen.this.menuListWidget.setSelected(this);
            if (MenuSelectionScreen.this.nextButton != null) {
                MenuSelectionScreen.this.nextButton.active = true;
            }
            return true;
        }

        @Override
        public Text getNarration() {
            return this.menuKind.label();
        }
    }
}
