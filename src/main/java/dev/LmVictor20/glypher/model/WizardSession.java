package dev.LmVictor20.glypher.model;

import dev.LmVictor20.glypher.gui.WizardView;
import dev.LmVictor20.glypher.menu.MenuCategory;
import dev.LmVictor20.glypher.menu.MenuTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WizardSession {
    private final UUID playerId;
    private WizardView view;
    private MenuCategory category;
    private MenuTemplate template;
    private String glyph;
    private int offsetX;
    private String editingPresetId;
    private int listPage;
    private List<String> visiblePresetIds;
    private String deleteTargetPresetId;
    private boolean awaitingGlyphChat;

    public WizardSession(UUID playerId) {
        this.playerId = playerId;
        this.view = WizardView.CATEGORY_SELECT;
        this.visiblePresetIds = new ArrayList<>();
        this.awaitingGlyphChat = false;
    }

    public UUID playerId() {
        return playerId;
    }

    public WizardView view() {
        return view;
    }

    public void view(WizardView view) {
        this.view = view;
    }

    public MenuCategory category() {
        return category;
    }

    public void category(MenuCategory category) {
        this.category = category;
    }

    public MenuTemplate template() {
        return template;
    }

    public void template(MenuTemplate template) {
        this.template = template;
    }

    public String glyph() {
        return glyph;
    }

    public void glyph(String glyph) {
        this.glyph = glyph;
    }

    public int offsetX() {
        return offsetX;
    }

    public void offsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public String editingPresetId() {
        return editingPresetId;
    }

    public void editingPresetId(String editingPresetId) {
        this.editingPresetId = editingPresetId;
    }

    public int listPage() {
        return listPage;
    }

    public void listPage(int listPage) {
        this.listPage = listPage;
    }

    public List<String> visiblePresetIds() {
        return visiblePresetIds;
    }

    public void visiblePresetIds(List<String> visiblePresetIds) {
        this.visiblePresetIds = visiblePresetIds;
    }

    public String deleteTargetPresetId() {
        return deleteTargetPresetId;
    }

    public void deleteTargetPresetId(String deleteTargetPresetId) {
        this.deleteTargetPresetId = deleteTargetPresetId;
    }

    public boolean awaitingGlyphChat() {
        return awaitingGlyphChat;
    }

    public void awaitingGlyphChat(boolean awaitingGlyphChat) {
        this.awaitingGlyphChat = awaitingGlyphChat;
    }
}