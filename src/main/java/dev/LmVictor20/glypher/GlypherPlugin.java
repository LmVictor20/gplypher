package dev.LmVictor20.glypher;

import dev.LmVictor20.glypher.command.MenuConfigCommand;
import dev.LmVictor20.glypher.listener.GlyphChatListener;
import dev.LmVictor20.glypher.listener.GlypherInventoryListener;
import dev.LmVictor20.glypher.listener.PlayerLifecycleListener;
import dev.LmVictor20.glypher.service.MenuPresetStorage;
import dev.LmVictor20.glypher.service.MenuWizardController;
import dev.LmVictor20.glypher.service.ProtocolBridge;
import dev.LmVictor20.glypher.service.ShiftCodec;
import dev.LmVictor20.glypher.service.WizardSessionManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class GlypherPlugin extends JavaPlugin {
    private WizardSessionManager sessionManager;
    private ProtocolBridge protocolBridge;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ShiftCodec shiftCodec = new ShiftCodec();
        MenuPresetStorage storage = new MenuPresetStorage(this, shiftCodec);
        storage.load();

        sessionManager = new WizardSessionManager();
        protocolBridge = new ProtocolBridge(this);
        MenuWizardController controller = new MenuWizardController(this, sessionManager, storage, shiftCodec, protocolBridge);

        MenuConfigCommand commandHandler = new MenuConfigCommand(controller);
        PluginCommand command = getCommand("menuconfig");
        if (command == null) {
            throw new IllegalStateException("Command 'menuconfig' is missing in plugin.yml");
        }
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        getServer().getPluginManager().registerEvents(new GlypherInventoryListener(controller), this);
        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(controller), this);
        getServer().getPluginManager().registerEvents(new GlyphChatListener(this, controller), this);

        getLogger().info("glypher enabled");
    }

    @Override
    public void onDisable() {
        if (protocolBridge != null) {
            protocolBridge.shutdown();
        }
        if (sessionManager != null) {
            sessionManager.clear();
        }
        getLogger().info("glypher disabled");
    }
}