package com.lmvictor20.glypher.repository;

import com.google.gson.reflect.TypeToken;
import com.lmvictor20.glypher.model.GlypherCatalog;
import java.lang.reflect.Type;
import java.nio.file.Path;
import net.minecraft.text.Text;

public final class GlypherCatalogRepository extends JsonRepositorySupport {
    private static final Type TYPE = new TypeToken<GlypherCatalog>() {
    }.getType();

    private GlypherCatalog cachedCatalog;

    public GlypherCatalogRepository(Path baseDir) {
        super(baseDir.resolve("catalog.json"));
    }

    public GlypherCatalog loadCatalog() {
        if (this.cachedCatalog == null) {
            GlypherCatalog loaded = this.readOrCreate(
                TYPE,
                GlypherCatalog.defaultCatalog(),
                Text.translatable("message.glypher.catalog_created", this.filePath().toString()),
                Text.translatable("message.glypher.catalog_recovered")
            );
            this.cachedCatalog = loaded == null ? GlypherCatalog.defaultCatalog() : loaded.normalized();
        }

        return this.cachedCatalog;
    }

    public void saveCatalog(GlypherCatalog catalog) {
        this.cachedCatalog = catalog.normalized();
        this.write(this.cachedCatalog);
    }
}
