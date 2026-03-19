package com.lmvictor20.glypher;

import com.lmvictor20.glypher.command.GlypherCommands;
import com.lmvictor20.glypher.service.GlypherServices;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GlypherClient implements ClientModInitializer {
    public static final String MOD_ID = "glypher";
    public static final Logger LOGGER = LoggerFactory.getLogger("Glypher");

    private static GlypherServices services;

    @Override
    public void onInitializeClient() {
        services = new GlypherServices(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID));
        GlypherCommands.register(services);
    }

    public static GlypherServices services() {
        return services;
    }
}

