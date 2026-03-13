package dev.LmVictor20.glypher.service;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ProtocolBridge {
    private static final int[] CONTROL_HOTBAR_SLOTS = {0, 1, 4, 7, 8};

    private final Plugin plugin;
    private final ProtocolManager protocolManager;
    private final Map<UUID, Integer> containerIds = new ConcurrentHashMap<>();

    public ProtocolBridge(Plugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListeners();
    }

    public void shutdown() {
        protocolManager.removePacketListeners(plugin);
        containerIds.clear();
    }

    public void forgetPlayer(UUID playerId) {
        containerIds.remove(playerId);
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
}