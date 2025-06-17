package com.jinzo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.Bukkit;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class KillCounter extends JavaPlugin implements Listener {

    // Text to display in the weapon lore
    private static final String KILL_COUNTER_KEY = ChatColor.RED + "Players killed: ";

    // List of valid weapons to track
    private static final Set<Material> TRACKED_ITEMS = Set.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.BOW
    );

    // Map to hold kills for each weapon by weapon UUID to set of player UUIDs killed
    private Map<String, Set<UUID>> killData = new HashMap<>();

    private final Gson gson = new Gson();
    private File dataFile;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        dataFile = new File(getDataFolder(), "killData.json");

        if (!dataFile.exists()) {
            try {
                getDataFolder().mkdirs();
                dataFile.createNewFile();
                saveKillData();
            } catch (Exception e) {
                getLogger().warning("Failed to create killData.json");
                e.printStackTrace();
            }
        }

        loadKillData();
    }

    @Override
    public void onDisable() {
        saveKillData();
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        ItemStack brokenItem = event.getBrokenItem();

        if (brokenItem == null || !TRACKED_ITEMS.contains(brokenItem.getType())) return;

        String key = getWeaponKey(brokenItem);
        if (killData.containsKey(key)) {
            killData.remove(key);
            saveKillData();
            getLogger().info("Removed kill data for broken item with key: " + key);
        }
    }

    private void loadKillData() {
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Set<UUID>>>() {}.getType();
            Map<String, List<String>> rawData = gson.fromJson(reader, Map.class);

            if (rawData == null) {
                killData = new HashMap<>();
                return;
            }

            Map<String, Set<UUID>> loadedData = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : rawData.entrySet()) {
                Set<UUID> uuids = new HashSet<>();
                for (String uuidStr : entry.getValue()) {
                    try {
                        uuids.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException ignored) {}
                }
                loadedData.put(entry.getKey(), uuids);
            }
            killData = loadedData;
        } catch (Exception e) {
            getLogger().warning("Failed to load killData.json");
            e.printStackTrace();
        }
    }

    private void saveKillData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, List<String>> serializableMap = new HashMap<>();
            for (Map.Entry<String, Set<UUID>> entry : killData.entrySet()) {
                List<String> uuidStrings = new ArrayList<>();
                for (UUID uuid : entry.getValue()) {
                    uuidStrings.add(uuid.toString());
                }
                serializableMap.put(entry.getKey(), uuidStrings);
            }
            gson.toJson(serializableMap, writer);
        } catch (Exception e) {
            getLogger().warning("Failed to save killData.json");
            e.printStackTrace();
        }
    }

    private String getWeaponKey(ItemStack weapon) {
        if (weapon == null) return "";

        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) return "";

        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(this, "weapon-uuid");

        String uuidString = data.get(key, PersistentDataType.STRING);
        if (uuidString == null) {
            UUID uuid = UUID.randomUUID();
            uuidString = uuid.toString();
            data.set(key, PersistentDataType.STRING, uuidString);
            weapon.setItemMeta(meta);
        }

        return uuidString;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;

        RegionScheduler scheduler = Bukkit.getRegionScheduler();
        scheduler.execute(this, killer.getLocation(), () -> {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (weapon == null || weapon.getType().isAir()) return;
            if (!TRACKED_ITEMS.contains(weapon.getType())) return;

            String key = getWeaponKey(weapon);
            Set<UUID> players = killData.getOrDefault(key, new HashSet<>());
            if (!players.add(dead.getUniqueId())) return;

            killData.put(key, players);
            updateWeaponLore(weapon, players.size());
            saveKillData();
        });
    }

    private void updateWeaponLore(ItemStack weapon, int killCount) {
        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        int lineIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (ChatColor.stripColor(lore.get(i)).startsWith("Players killed: ")) {
                lineIndex = i;
                break;
            }
        }

        String newCounterLine = KILL_COUNTER_KEY + killCount;

        if (lineIndex >= 0) {
            lore.set(lineIndex, newCounterLine);
        } else {
            lore.add(newCounterLine);
        }

        meta.setLore(lore);
        weapon.setItemMeta(meta);
    }
}
