package dev.LmVictor20.glypher.service;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ProtocolBridge {
    private static final int[] CONTROL_HOTBAR_SLOTS = {0, 1, 4, 7, 8};

    private final Plugin plugin;
    private final ProtocolManager protocolManager;

    private final Map<UUID, SignRequest> signRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> containerIds = new ConcurrentHashMap<>();

    public ProtocolBridge(Plugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListeners();
    }

    public void shutdown() {
        protocolManager.removePacketListeners(plugin);
        signRequests.clear();
        containerIds.clear();
    }

    public void forgetPlayer(UUID playerId) {
        signRequests.remove(playerId);
        containerIds.remove(playerId);
    }

    public void requestVirtualSignInput(Player player, String hintLine, Consumer<String[]> onComplete) {
        Location signLocation = player.getLocation().getBlock().getLocation().add(0, 5, 0);
        BlockData originalData = signLocation.getBlock().getBlockData();

        player.sendBlockChange(signLocation, Material.OAK_SIGN.createBlockData());
        try {
            player.sendSignChange(signLocation, new String[]{hintLine, "", "", ""});
        } catch (IllegalArgumentException ignored) {
            player.sendSignChange(signLocation, new String[]{"Enter 1 glyph", "", "", ""});
        }

        signRequests.put(player.getUniqueId(), new SignRequest(signLocation, originalData, onComplete));
        sendOpenSignEditor(player, signLocation);
    }

    public void showHotbarControls(Player player, InventoryView view) {
        Integer windowId = containerIds.get(player.getUniqueId());
        if (windowId == null) {
            return;
        }

        int topSize = view.getTopInventory().getSize();

        sendSetSlot(player, windowId, topSize + 27, controlItem(Material.RED_STAINED_GLASS_PANE, "<- 4 px"));
        sendSetSlot(player, windowId, topSize + 28, controlItem(Material.RED_STAINED_GLASS, "<- 1 px"));
        sendSetSlot(player, windowId, topSize + 31, controlItem(Material.LIME_DYE, "Save"));
        sendSetSlot(player, windowId, topSize + 34, controlItem(Material.LIME_STAINED_GLASS, "+1 px ->"));
        sendSetSlot(player, windowId, topSize + 35, controlItem(Material.LIME_STAINED_GLASS_PANE, "+4 px ->"));
    }

    public void clearHotbarControls(Player player, InventoryView view) {
        Integer windowId = containerIds.get(player.getUniqueId());
        if (windowId == null) {
            player.updateInventory();
            return;
        }

        int topSize = view.getTopInventory().getSize();
        for (int hotbarSlot : CONTROL_HOTBAR_SLOTS) {
            ItemStack realItem = player.getInventory().getItem(hotbarSlot);
            sendSetSlot(player, windowId, topSize + 27 + hotbarSlot, realItem);
        }
        player.updateInventory();
    }

    private void registerPacketListeners() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
            PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                SignRequest request = signRequests.remove(player.getUniqueId());
                if (request == null) {
                    return;
                }

                String[] lines = readSignLines(event.getPacket());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendBlockChange(request.location(), request.originalData());
                    request.onComplete().accept(lines);
                });
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
            PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                StructureModifier<Integer> ints = event.getPacket().getIntegers();
                if (ints.size() > 0) {
                    containerIds.put(event.getPlayer().getUniqueId(), ints.read(0));
                }
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
            PacketType.Play.Server.CLOSE_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                containerIds.remove(event.getPlayer().getUniqueId());
            }
        });
    }

    private void sendOpenSignEditor(Player player, Location location) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        packet.getBlockPositionModifier().write(0,
            new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        StructureModifier<Boolean> booleans = packet.getBooleans();
        if (booleans.size() > 0) {
            booleans.write(0, true);
        }

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to send OPEN_SIGN_EDITOR packet: " + exception.getMessage());
        }
    }

    private void sendSetSlot(Player player, int windowId, int rawSlot, ItemStack itemStack) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);

        StructureModifier<Integer> ints = packet.getIntegers();
        if (ints.size() > 0) {
            ints.write(0, windowId);
        }
        if (ints.size() > 1) {
            ints.write(1, 0);
        }
        if (ints.size() > 2) {
            ints.write(2, rawSlot);
        }

        StructureModifier<Short> shorts = packet.getShorts();
        if (shorts.size() > 0) {
            shorts.write(0, (short) rawSlot);
        }

        ItemStack safeItem = itemStack == null ? new ItemStack(Material.AIR) : itemStack.clone();
        packet.getItemModifier().write(0, safeItem);

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to send SET_SLOT packet: " + exception.getMessage());
        }
    }

    private String[] readSignLines(PacketContainer packet) {
        StructureModifier<String[]> arrays = packet.getStringArrays();
        if (arrays.size() > 0) {
            String[] result = arrays.read(0);
            if (result != null) {
                return result;
            }
        }

        StructureModifier<String> strings = packet.getStrings();
        if (strings.size() >= 4) {
            return new String[]{
                strings.read(0),
                strings.read(1),
                strings.read(2),
                strings.read(3)
            };
        }

        return new String[0];
    }

    private ItemStack controlItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList("Click to control offset"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private record SignRequest(Location location, BlockData originalData, Consumer<String[]> onComplete) {
    }
}