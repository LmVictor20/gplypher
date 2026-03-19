package com.lmvictor20.glypher.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.lmvictor20.glypher.GlypherClient;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

abstract class JsonRepositorySupport {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path filePath;

    JsonRepositorySupport(Path filePath) {
        this.filePath = filePath;
    }

    protected Path filePath() {
        return this.filePath;
    }

    protected <T> T readOrCreate(Type type, T defaultValue, Text createdMessage, Text recoveryMessage) {
        try {
            Files.createDirectories(this.filePath.getParent());

            if (!Files.exists(this.filePath)) {
                this.write(defaultValue);
                if (!createdMessage.getString().isBlank()) {
                    this.sendChatMessage(createdMessage);
                }
                return defaultValue;
            }

            return GSON.fromJson(Files.readString(this.filePath), type);
        } catch (IOException | JsonParseException exception) {
            GlypherClient.LOGGER.warn("Glypher could not read {}. Falling back to defaults.", this.filePath, exception);
            this.backupBrokenFile();
            if (!recoveryMessage.getString().isBlank()) {
                this.sendChatMessage(recoveryMessage);
            }
            this.write(defaultValue);
            return defaultValue;
        }
    }

    protected void write(Object value) {
        try {
            Files.createDirectories(this.filePath.getParent());
            Path tempFile = this.filePath.resolveSibling(this.filePath.getFileName() + ".tmp");
            Files.writeString(tempFile, GSON.toJson(value));
            Files.move(tempFile, this.filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write Glypher data to " + this.filePath, exception);
        }
    }

    private void backupBrokenFile() {
        if (!Files.exists(this.filePath)) {
            return;
        }

        try {
            Files.move(
                this.filePath,
                this.filePath.resolveSibling(this.filePath.getFileName() + ".broken-" + Instant.now().toEpochMilli()),
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            GlypherClient.LOGGER.warn("Glypher could not back up broken file {}", this.filePath, exception);
        }
    }

    private void sendChatMessage(Text message) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(message, false);
            }
        } catch (Throwable ignored) {
        }
    }
}
