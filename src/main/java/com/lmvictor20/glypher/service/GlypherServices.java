package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.model.MenuKind;
import com.lmvictor20.glypher.model.SavedLayout;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import com.lmvictor20.glypher.repository.GlypherCatalogRepository;
import com.lmvictor20.glypher.repository.GlypherLayoutsRepository;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class GlypherServices {
    private final GlypherCatalogRepository catalogRepository;
    private final GlypherLayoutsRepository layoutsRepository;
    private final GlypherSessionManager sessionManager;
    private final TitleComposer titleComposer;
    private final GlypherPrinter printer;
    private final ActiveShiftGlyphLoader shiftGlyphLoader;

    public GlypherServices(Path configDir) {
        this.catalogRepository = new GlypherCatalogRepository(configDir);
        this.layoutsRepository = new GlypherLayoutsRepository(configDir);
        this.sessionManager = new GlypherSessionManager();
        this.titleComposer = new TitleComposer();
        this.printer = new GlypherPrinter(this.titleComposer);
        this.shiftGlyphLoader = new ActiveShiftGlyphLoader();
    }

    public GlypherCatalog catalog() {
        GlypherCatalog baseCatalog = this.catalogRepository.loadCatalog();
        var runtimeShiftGlyphs = this.shiftGlyphLoader.loadShiftGlyphs();
        if (runtimeShiftGlyphs.isEmpty()) {
            return baseCatalog;
        }

        return new GlypherCatalog(baseCatalog.yPresets(), runtimeShiftGlyphs);
    }

    public List<SavedLayout> layouts() {
        return this.layoutsRepository.loadLayouts();
    }

    public Optional<SavedLayout> findLayout(String id) {
        return this.layoutsRepository.findById(id);
    }

    public GlypherSession currentSession() {
        return this.sessionManager.currentSession();
    }

    public GlypherSession resetSession() {
        return this.sessionManager.reset();
    }

    public GlypherSession replaceSession(GlypherSession session) {
        return this.sessionManager.replace(session);
    }

    public TitleCompositionResult composeTitle(GlypherSession session) {
        return this.titleComposer.compose(this.catalog(), session.rawGlyphText(), session.xOffset());
    }

    public GlypherSession newSessionForMenu(MenuKind menuKind) {
        GlypherSession session = this.resetSession();
        session.setMenuKind(menuKind);
        return session;
    }

    public GlypherPrinter printer() {
        return this.printer;
    }

    public SavedLayout saveLayout(String layoutId, GlypherSession session) {
        TitleCompositionResult composition = this.composeTitle(session);
        if (!composition.valid()) {
            throw new IllegalStateException("Cannot save invalid title composition");
        }

        long now = System.currentTimeMillis();
        long createdAt = this.findLayout(layoutId).map(SavedLayout::createdAt).orElse(now);
        SavedLayout layout = new SavedLayout(
            layoutId,
            session.menuKind(),
            session.rawGlyphText(),
            session.xOffset(),
            session.titleAscent(),
            composition.finalTitle(),
            createdAt,
            now
        );
        this.layoutsRepository.saveLayout(layout);
        GlypherSession current = this.currentSession();
        current.setLayoutId(layoutId);
        return layout;
    }

    public void deleteLayout(String layoutId) {
        this.layoutsRepository.deleteLayout(layoutId);
    }
}
