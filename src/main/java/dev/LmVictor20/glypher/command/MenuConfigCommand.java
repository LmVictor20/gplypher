package dev.LmVictor20.glypher.command;

import dev.LmVictor20.glypher.service.MenuWizardController;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuConfigCommand implements CommandExecutor, TabCompleter {
    private final MenuWizardController controller;

    public MenuConfigCommand(MenuWizardController controller) {
        this.controller = controller;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("glypher.menuconfig.use")) {
                player.sendMessage("У вас нет права glypher.menuconfig.use");
                return true;
            }
            controller.startNewWizard(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("list") || sub.equals("saved")) {
            controller.openSavedList(player, 0);
            return true;
        }

        player.sendMessage("Использование: /menuconfig [list|saved]");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("list", "saved").stream()
                .filter(value -> value.startsWith(args[0].toLowerCase()))
                .toList();
        }
        return List.of();
    }
}
