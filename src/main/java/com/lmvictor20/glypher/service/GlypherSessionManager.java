package com.lmvictor20.glypher.service;

import com.lmvictor20.glypher.model.GlypherSession;

public final class GlypherSessionManager {
    private GlypherSession currentSession;

    public GlypherSessionManager() {
        this.currentSession = new GlypherSession();
    }

    public GlypherSession currentSession() {
        return this.currentSession;
    }

    public GlypherSession reset() {
        this.currentSession = new GlypherSession();
        return this.currentSession;
    }

    public GlypherSession replace(GlypherSession session) {
        this.currentSession = session.copy();
        return this.currentSession;
    }
}
