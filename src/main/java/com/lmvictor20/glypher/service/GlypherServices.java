package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.ActiveGlyphMetrics;
import com.lmvictor20.glypher.model.GlypherCatalog;
import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.model.MenuKind;
import com.lmvictor20.glypher.model.ResourcePackProvider;
import com.lmvictor20.glypher.model.SavedLayout;
import com.lmvictor20.glypher.model.TitleCompositionResult;
import com.lmvictor20.glypher.repository.GlypherCatalogRepository;
import com.lmvictor20.glypher.repository.GlypherExportRepository;
import com.lmvictor20.glypher.repository.GlypherLayoutsRepository;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class GlypherServices {
    private final GlypherCatalogRepository catalogRepository;
    private final GlypherExportRepository exportRepository;
    private final GlypherLayoutsRepository layoutsRepository;
    private final GlypherSessionManager sessionManager;
    private final TitleComposer titleComposer;
    private final ActiveDefaultFontMetricsLoader defaultFontMetricsLoader;

    public GlypherServices(Path configDir) {
        this.catalogRepository = new GlypherCatalogRepository(configDir);
        this.exportRepository = new GlypherExportRepository(configDir);
        this.layoutsRepository = new GlypherLayoutsRepository(configDir);
        this.sessionManager = new GlypherSessionManager();
        this.titleComposer = new TitleComposer();
        this.defaultFontMetricsLoader = new ActiveDefaultFontMetricsLoader();
    }

    public GlypherCatalog catalog() {
        GlypherCatalog baseCatalog = this.catalogRepository.loadCatalog();
        return new GlypherCatalog(baseCatalog.yPresets(), ShiftGlyphPalette.glyphs());
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
        return this.titleComposer.compose(this.catalog(), session.provider(), session.rawGlyphText(), session.xOffset());
    }

    public GlypherSession newSessionForMenu(MenuKind menuKind, ResourcePackProvider provider) {
        GlypherSession session = this.resetSession();
        session.setMenuKind(menuKind);
        session.setProvider(provider);
        return session;
    }

    public Optional<ActiveGlyphMetrics> detectSelectedGlyphMetrics(GlypherSession session) {
        return this.defaultFontMetricsLoader.findMetrics(session.rawGlyphText());
    }

    public int recommendedFontAscent(GlypherSession session) {
        return this.detectSelectedGlyphMetrics(session)
            .map(metrics -> metrics.recommendedAscent(session.titleAscent()))
            .orElse(session.titleAscent());
    }

    public Path exportLayout(SavedLayout layout) {
        return this.exportRepository.exportLayout(layout, this.recommendedFontAscent(layout.toSession()));
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
            session.provider(),
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
        this.exportRepository.deleteExport(layoutId);
    }
}
