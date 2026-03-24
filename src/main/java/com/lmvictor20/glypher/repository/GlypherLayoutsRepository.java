package com.lmvictor20.glypher.repository;

import com.google.gson.reflect.TypeToken;
import com.lmvictor20.glypher.model.SavedLayout;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;

public final class GlypherLayoutsRepository extends JsonRepositorySupport {
    private static final Type TYPE = new TypeToken<List<SavedLayout>>() {
    }.getType();

    private List<SavedLayout> cachedLayouts;

    public GlypherLayoutsRepository(Path baseDir) {
        super(baseDir.resolve("layouts.json"));
    }

    public List<SavedLayout> loadLayouts() {
        if (this.cachedLayouts == null) {
            List<SavedLayout> loaded = this.readOrCreate(
                TYPE,
                new ArrayList<>(),
                Text.empty(),
                Text.translatable("message.glypher.layouts_recovered")
            );
            this.cachedLayouts = loaded == null
                ? new ArrayList<>()
                : loaded.stream()
                    .filter(layout -> layout != null)
                    .map(SavedLayout::normalized)
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            this.cachedLayouts.sort(Comparator.comparingLong(SavedLayout::updatedAt).reversed());
        }

        return this.cachedLayouts;
    }

    public Optional<SavedLayout> findById(String id) {
        return this.loadLayouts().stream().filter(layout -> layout.id().equalsIgnoreCase(id)).findFirst();
    }

    public void saveLayout(SavedLayout layout) {
        List<SavedLayout> layouts = new ArrayList<>(this.loadLayouts());
        layouts.removeIf(existing -> existing.id().equalsIgnoreCase(layout.id()));
        layouts.add(layout);
        layouts.sort(Comparator.comparingLong(SavedLayout::updatedAt).reversed());
        this.cachedLayouts = layouts;
        this.write(layouts);
    }

    public void deleteLayout(String id) {
        List<SavedLayout> layouts = new ArrayList<>(this.loadLayouts());
        layouts.removeIf(existing -> existing.id().equalsIgnoreCase(id));
        this.cachedLayouts = layouts;
        this.write(layouts);
    }
}
