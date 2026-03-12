package dev.LmVictor20.glypher.service;

import dev.LmVictor20.glypher.gui.GlypherInventoryHolder;
import dev.LmVictor20.glypher.gui.WizardView;
import dev.LmVictor20.glypher.menu.MenuCategory;
import dev.LmVictor20.glypher.menu.MenuTemplate;
import dev.LmVictor20.glypher.model.SavedMenuPreset;
import dev.LmVictor20.glypher.model.WizardSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class MenuWizardController {
    private static final int PAGE_SIZE = 45;
    private static final int MIN_OFFSET = ShiftCodec.MIN_OFFSET;
    private static final int MAX_OFFSET = ShiftCodec.MAX_OFFSET;

    private final Plugin plugin;
    private final WizardSessionManager sessionManager;
    private final MenuPresetStorage storage;
    private final ShiftCodec shiftCodec;
    private final ProtocolBridge protocolBridge;

    public MenuWizardController(
        Plugin plugin,
        WizardSessionManager sessionManager,
        MenuPresetStorage storage,
        ShiftCodec shiftCodec,
        ProtocolBridge protocolBridge
    ) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
        this.storage = storage;
        this.shiftCodec = shiftCodec;
        this.protocolBridge = protocolBridge;
    }

    public void startNewWizard(Player player) {
        WizardSession session = sessionManager.getOrCreate(player.getUniqueId());
        session.view(WizardView.CATEGORY_SELECT);
        session.category(null);
        session.template(null);
        session.glyph(null);
        session.offsetX(0);
        session.editingPresetId(null);
        session.deleteTargetPresetId(null);
        session.visiblePresetIds(Collections.emptyList());
        openCategoryMenu(player, session);
    }

    public void openSavedList(Player player, int page) {
        if (!player.hasPermission("glypher.menuconfig.list")) {
            player.sendMessage(Component.text("У вас нет права glypher.menuconfig.list"));
            return;
        }

        WizardSession session = sessionManager.getOrCreate(player.getUniqueId());
        session.view(WizardView.SAVED_LIST);

        List<SavedMenuPreset> all = storage.listSorted();
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        int from = safePage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());

        List<SavedMenuPreset> pageItems = from >= to ? List.of() : all.subList(from, to);
        List<String> visibleIds = new ArrayList<>(pageItems.size());
        for (SavedMenuPreset preset : pageItems) {
            visibleIds.add(preset.id());
        }

        session.listPage(safePage);
        session.visiblePresetIds(visibleIds);

        GlypherInventoryHolder holder = new GlypherInventoryHolder(player.getUniqueId(), WizardView.SAVED_LIST);
        Inventory inventory = Bukkit.createInventory(holder, 54,
            Component.text("Сохранённые меню | стр. " + (safePage + 1) + "/" + totalPages));

        fillInventory(inventory, namedItem(Material.GRAY_STAINED_GLASS_PANE, " "));

        for (int i = 0; i < pageItems.size(); i++) {
            SavedMenuPreset preset = pageItems.get(i);
            inventory.setItem(i, presetItem(preset));
        }

        if (safePage > 0) {
            inventory.setItem(45, namedItem(Material.ARROW, "Предыдущая страница"));
        }
        inventory.setItem(49, namedItem(Material.BARRIER, "Закрыть"));
        if (safePage < totalPages - 1) {
            inventory.setItem(53, namedItem(Material.ARROW, "Следующая страница"));
        }

        player.openInventory(inventory);
    }

    public void handleCategoryClick(Player player, int slot) {
        WizardSession session = sessionManager.getOrCreate(player.getUniqueId());
        MenuCategory category = switch (slot) {
            case 10 -> MenuCategory.CHESTS;
            case 12 -> MenuCategory.FURNACES;
            case 14 -> MenuCategory.WORKSTATIONS;
            case 16 -> MenuCategory.MISC;
            default -> null;
        };

        if (category == null) {
            return;
        }

        session.category(category);
        session.view(WizardView.TEMPLATE_SELECT);
        openTemplateMenu(player, session);
    }

    public void handleTemplateClick(Player player, int slot) {
        WizardSession session = sessionManager.get(player.getUniqueId()).orElse(null);
        if (session == null || session.category() == null) {
            startNewWizard(player);
            return;
        }

        List<MenuTemplate> templates = MenuTemplate.byCategory(session.category());
        if (slot < 0 || slot >= templates.size()) {
            return;
        }

        MenuTemplate template = templates.get(slot);
        session.template(template);
        session.offsetX(0);

        requestGlyphViaVirtualSign(player, session);
    }

    public void handlePreviewClick(Player player, InventoryView view, int slot, ClickType clickType) {
        WizardSession session = sessionManager.get(player.getUniqueId()).orElse(null);
        if (session == null || session.template() == null) {
            return;
        }

        if (session.view() == WizardView.PREVIEW_READONLY) {
            return;
        }

        if (slot == 0) {
            session.offsetX(shiftCodec.clampOffset(session.offsetX() - 4));
            reopenPreview(player, session, true);
            return;
        }

        if (slot == 1) {
            session.offsetX(shiftCodec.clampOffset(session.offsetX() - 1));
            reopenPreview(player, session, true);
            return;
        }

        if (slot == 4) {
            saveCurrentPreset(player, session);
            return;
        }

        if (slot == 7) {
            session.offsetX(shiftCodec.clampOffset(session.offsetX() + 1));
            reopenPreview(player, session, true);
            return;
        }

        if (slot == 8) {
            session.offsetX(shiftCodec.clampOffset(session.offsetX() + 4));
            reopenPreview(player, session, true);
        }
    }

    public void handleSavedListClick(Player player, int slot, ClickType clickType, boolean shiftClick) {
        WizardSession session = sessionManager.get(player.getUniqueId()).orElse(null);
        if (session == null) {
            openSavedList(player, 0);
            return;
        }

        if (slot == 45) {
            openSavedList(player, session.listPage() - 1);
            return;
        }
        if (slot == 53) {
            openSavedList(player, session.listPage() + 1);
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot < 0 || slot >= PAGE_SIZE) {
            return;
        }

        List<String> visibleIds = session.visiblePresetIds();
        if (slot >= visibleIds.size()) {
            return;
        }

        String presetId = visibleIds.get(slot);
        Optional<SavedMenuPreset> presetOptional = storage.get(presetId);
        if (presetOptional.isEmpty()) {
            player.sendMessage(Component.text("Запись больше не существует."));
            openSavedList(player, session.listPage());
            return;
        }

        SavedMenuPreset preset = presetOptional.get();
        if (shiftClick && clickType.isRightClick()) {
            if (!player.hasPermission("glypher.menuconfig.delete")) {
                player.sendMessage(Component.text("У вас нет права glypher.menuconfig.delete"));
                return;
            }
            openDeleteConfirm(player, session, preset.id());
            return;
        }

        if (clickType.isRightClick()) {
            startEditingPreset(player, session, preset);
            return;
        }

        if (clickType.isLeftClick()) {
            openPresetPreview(player, session, preset);
        }
    }

    public void handleDeleteConfirmClick(Player player, int slot) {
        WizardSession session = sessionManager.get(player.getUniqueId()).orElse(null);
        if (session == null) {
            openSavedList(player, 0);
            return;
        }

        String targetId = session.deleteTargetPresetId();
        if (targetId == null) {
            openSavedList(player, session.listPage());
            return;
        }

        if (slot == 11) {
            boolean removed = storage.delete(targetId);
            player.sendMessage(Component.text(removed
                ? "Запись " + targetId + " удалена."
                : "Не удалось удалить: запись не найдена."));
            session.deleteTargetPresetId(null);
            openSavedList(player, session.listPage());
            return;
        }

        if (slot == 15 || slot == 22) {
            session.deleteTargetPresetId(null);
            openSavedList(player, session.listPage());
        }
    }

    public void onPreviewClosed(Player player, InventoryView view) {
        protocolBridge.clearHotbarControls(player, view);
    }

    public void cleanupPlayer(Player player) {
        protocolBridge.forgetPlayer(player.getUniqueId());
        sessionManager.remove(player.getUniqueId());
    }

    private void openCategoryMenu(Player player, WizardSession session) {
        GlypherInventoryHolder holder = new GlypherInventoryHolder(player.getUniqueId(), WizardView.CATEGORY_SELECT);
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text("Шаг 1/3: Выберите категорию"));
        fillInventory(inventory, namedItem(Material.GRAY_STAINED_GLASS_PANE, " "));

        inventory.setItem(10, namedItem(MenuCategory.CHESTS.icon(), MenuCategory.CHESTS.displayName()));
        inventory.setItem(12, namedItem(MenuCategory.FURNACES.icon(), MenuCategory.FURNACES.displayName()));
        inventory.setItem(14, namedItem(MenuCategory.WORKSTATIONS.icon(), MenuCategory.WORKSTATIONS.displayName()));
        inventory.setItem(16, namedItem(MenuCategory.MISC.icon(), MenuCategory.MISC.displayName()));

        session.view(WizardView.CATEGORY_SELECT);
        player.openInventory(inventory);
    }

    private void openTemplateMenu(Player player, WizardSession session) {
        List<MenuTemplate> templates = MenuTemplate.byCategory(session.category());

        GlypherInventoryHolder holder = new GlypherInventoryHolder(player.getUniqueId(), WizardView.TEMPLATE_SELECT);
        Inventory inventory = Bukkit.createInventory(holder, 54,
            Component.text("Шаг 2/3: Тип меню - " + session.category().displayName()));

        fillInventory(inventory, namedItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        for (int i = 0; i < templates.size() && i < 45; i++) {
            MenuTemplate template = templates.get(i);
            inventory.setItem(i, namedItem(template.icon(), template.displayName()));
        }

        session.view(WizardView.TEMPLATE_SELECT);
        player.openInventory(inventory);
    }

    private void requestGlyphViaVirtualSign(Player player, WizardSession session) {
        player.closeInventory();
        player.sendMessage(Component.text("Шаг 3/3: Введите один символ-глиф в первую строку таблички."));

        protocolBridge.requestVirtualSignInput(player, "Вставьте 1 глиф", lines -> {
            Optional<String> glyphOptional = GlyphParser.firstCodePoint(lines);
            if (glyphOptional.isEmpty()) {
                player.sendMessage(Component.text("Не удалось прочитать символ. Повторите ввод."));
                requestGlyphViaVirtualSign(player, session);
                return;
            }

            session.glyph(glyphOptional.get());
            session.offsetX(shiftCodec.clampOffset(session.offsetX()));
            session.view(WizardView.PREVIEW_EDIT);
            reopenPreview(player, session, true);
        });
    }

    private void reopenPreview(Player player, WizardSession session, boolean editable) {
        if (session.template() == null || session.glyph() == null) {
            player.sendMessage(Component.text("Сессия повреждена. Запустите /menuconfig снова."));
            return;
        }

        String title = shiftCodec.buildTitle(session.glyph(), session.offsetX());
        GlypherInventoryHolder holder = new GlypherInventoryHolder(player.getUniqueId(),
            editable ? WizardView.PREVIEW_EDIT : WizardView.PREVIEW_READONLY);

        Inventory inventory;
        try {
            if (session.template().isChestSizeTemplate()) {
                inventory = Bukkit.createInventory(holder, session.template().chestSize(), Component.text(title));
            } else {
                inventory = Bukkit.createInventory(holder, session.template().inventoryType(), Component.text(title));
            }
        } catch (Exception exception) {
            player.sendMessage(Component.text("Не удалось открыть превью для этого типа: " + session.template().name()));
            plugin.getLogger().warning("Failed to open preview for " + session.template() + ": " + exception.getMessage());
            return;
        }

        fillInventory(inventory, namedItem(Material.PAPER, "Превью"));

        session.view(editable ? WizardView.PREVIEW_EDIT : WizardView.PREVIEW_READONLY);
        player.openInventory(inventory);

        if (editable) {
            player.sendMessage(Component.text("Сдвиг X: " + session.offsetX() + " (диапазон " + MIN_OFFSET + ".." + MAX_OFFSET + ")"));
            Bukkit.getScheduler().runTaskLater(plugin,
                () -> protocolBridge.showHotbarControls(player, player.getOpenInventory()), 2L);
        } else {
            player.sendMessage(Component.text("Режим просмотра. Закройте окно для возврата."));
        }
    }

    private void saveCurrentPreset(Player player, WizardSession session) {
        if (session.category() == null || session.template() == null || session.glyph() == null) {
            player.sendMessage(Component.text("Недостаточно данных для сохранения. Начните заново через /menuconfig."));
            return;
        }

        SavedMenuPreset preset;
        if (session.editingPresetId() == null) {
            preset = storage.saveNew(session.category(), session.template(), session.glyph(), session.offsetX());
        } else {
            preset = storage.upsert(session.editingPresetId(), session.category(), session.template(),
                session.glyph(), session.offsetX());
        }

        player.closeInventory();
        player.sendMessage(Component.text("Сохранено как " + preset.id() +
            " | " + preset.template().displayName() + " | offsetX=" + preset.offsetX()));
    }

    private void openDeleteConfirm(Player player, WizardSession session, String presetId) {
        session.view(WizardView.DELETE_CONFIRM);
        session.deleteTargetPresetId(presetId);

        GlypherInventoryHolder holder = new GlypherInventoryHolder(player.getUniqueId(), WizardView.DELETE_CONFIRM);
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text("Удалить " + presetId + "?"));
        fillInventory(inventory, namedItem(Material.GRAY_STAINED_GLASS_PANE, " "));

        inventory.setItem(11, namedItem(Material.GREEN_WOOL, "Подтвердить удаление"));
        inventory.setItem(15, namedItem(Material.RED_WOOL, "Отмена"));
        inventory.setItem(22, namedItem(Material.BARRIER, "Назад"));

        player.openInventory(inventory);
    }

    private void startEditingPreset(Player player, WizardSession session, SavedMenuPreset preset) {
        session.category(preset.category());
        session.template(preset.template());
        session.glyph(preset.glyph());
        session.offsetX(shiftCodec.clampOffset(preset.offsetX()));
        session.editingPresetId(preset.id());
        session.view(WizardView.PREVIEW_EDIT);
        reopenPreview(player, session, true);
    }

    private void openPresetPreview(Player player, WizardSession session, SavedMenuPreset preset) {
        session.category(preset.category());
        session.template(preset.template());
        session.glyph(preset.glyph());
        session.offsetX(shiftCodec.clampOffset(preset.offsetX()));
        session.editingPresetId(null);
        session.view(WizardView.PREVIEW_READONLY);
        reopenPreview(player, session, false);
    }

    private ItemStack presetItem(SavedMenuPreset preset) {
        ItemStack item = new ItemStack(preset.template().icon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(preset.id() + " | " + preset.template().displayName()));
            meta.lore(List.of(
                Component.text("Категория: " + preset.category().displayName()),
                Component.text("Тип: " + preset.template().name()),
                Component.text("Глиф: " + preset.glyph()),
                Component.text("Сдвиг X: " + preset.offsetX()),
                Component.text("ЛКМ: превью | ПКМ: редактировать | Shift+ПКМ: удалить")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack namedItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillInventory(Inventory inventory, ItemStack template) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, template.clone());
        }
    }
}
