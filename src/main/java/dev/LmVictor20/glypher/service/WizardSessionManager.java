package dev.LmVictor20.glypher.service;

import dev.LmVictor20.glypher.model.WizardSession;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WizardSessionManager {
    private final Map<UUID, WizardSession> sessions = new ConcurrentHashMap<>();

    public WizardSession getOrCreate(UUID playerId) {
        return sessions.computeIfAbsent(playerId, WizardSession::new);
    }

    public Optional<WizardSession> get(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public void remove(UUID playerId) {
        sessions.remove(playerId);
    }

    public void clear() {
        sessions.clear();
    }
}
