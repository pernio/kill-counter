package com.jinzo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KillCounter extends JavaPlugin implements Listener, TabExecutor {

    private static final String KILL_COUNTER_KEY = ChatColor.RED + "Players killed: ";
    private static final String LAST_KILLED_KEY = ChatColor.GOLD + "Last killed: ";

    private static final Set<Material> TRACKED_ITEMS = Set.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.BOW, Material.CROSSBOW, Material.MACE, Material.TRIDENT
    );

    // Use a thread-safe map for concurrency under Folia
    private final Map<String, WeaponKillData> killData = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();
    private File dataFile;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("lastKilled").setExecutor(this);

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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;

        // Schedule the task on Folia's region scheduler for thread safety
        RegionScheduler scheduler = Bukkit.getRegionScheduler();
        scheduler.execute(this, killer.getLocation(), () -> {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (weapon == null || weapon.getType().isAir()) return;
            if (!TRACKED_ITEMS.contains(weapon.getType())) return;

            String key = getWeaponKey(weapon);
            if (key.isEmpty()) {
                getLogger().warning("Failed to get weapon key for weapon on player " + killer.getName());
                return;
            }

            WeaponKillData data = killData.getOrDefault(key, new WeaponKillData());
            data.uniqueKills.add(dead.getUniqueId());
            data.lastKilled = dead.getUniqueId();

            killData.put(key, data);

            // Update lore safely on the main thread
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateWeaponLore(weapon, data.uniqueKills.size(), dead.getName());
                    saveKillData();
                }
            }.runTask(this);
        });
    }

    private void updateWeaponLore(ItemStack weapon, int killCount, String lastKilledName) {
        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        int killLineIndex = -1;
        int lastKilledLineIndex = -1;

        for (int i = 0; i < lore.size(); i++) {
            String plain = ChatColor.stripColor(lore.get(i));
            if (plain.startsWith("Players killed: ")) {
                killLineIndex = i;
            }
            if (plain.startsWith("Last killed: ")) {
                lastKilledLineIndex = i;
            }
        }

        String killCountLine = KILL_COUNTER_KEY + killCount;
        String lastKilledLine = LAST_KILLED_KEY + lastKilledName;

        if (killLineIndex >= 0) {
            lore.set(killLineIndex, killCountLine);
        } else {
            lore.add(killCountLine);
        }

        if (lastKilledLineIndex >= 0) {
            lore.set(lastKilledLineIndex, lastKilledLine);
        } else {
            lore.add(lastKilledLine);
        }

        meta.setLore(lore);
        weapon.setItemMeta(meta);
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

            // Setting ItemMeta must be done on main thread
            String finalUuidString = uuidString;
            new BukkitRunnable() {
                @Override
                public void run() {
                    weapon.setItemMeta(meta);
                    getLogger().info("Generated new UUID for weapon: " + finalUuidString);
                }
            }.runTask(this);
        }

        return uuidString;
    }

    private void loadKillData() {
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, WeaponKillData>>() {}.getType();
            Map<String, WeaponKillData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                killData.clear();
                killData.putAll(loaded);
            } else {
                killData.clear();
            }
        } catch (Exception e) {
            getLogger().warning("Failed to load killData.json");
            e.printStackTrace();
            killData.clear();
        }
    }

    private void saveKillData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(killData, writer);
        } catch (Exception e) {
            getLogger().warning("Failed to save killData.json");
            e.printStackTrace();
        }
    }

    // Command handler for /lastKilled
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir() || !TRACKED_ITEMS.contains(weapon.getType())) {
            player.sendMessage(ChatColor.RED + "You must hold a tracked weapon in your main hand.");
            return true;
        }

        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "This weapon has no metadata.");
            return true;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey uuidKey = new NamespacedKey(this, "weapon-uuid");
        String key = container.get(uuidKey, PersistentDataType.STRING);

        if (key == null || !killData.containsKey(key)) {
            player.sendMessage(ChatColor.YELLOW + "This weapon has no kill data.");
            return true;
        }

        if (key.isEmpty()) {
            player.sendMessage(ChatColor.RED + "This weapon has no data.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("lastKilled")) {
            WeaponKillData data = killData.get(key);
            if (data == null || data.lastKilled == null) {
                player.sendMessage(ChatColor.YELLOW + "No kills recorded yet for this weapon.");
                return true;
            }

            String lastKilledName = Bukkit.getOfflinePlayer(data.lastKilled).getName();
            player.sendMessage(ChatColor.GREEN + "Last player killed with this weapon: " + ChatColor.GOLD + lastKilledName);
            return true;
        }

        if (command.getName().equalsIgnoreCase("resetKills")) {
            // Remove from memory
            killData.remove(key);

            // Remove UUID metadata and lore lines
            container.remove(uuidKey);

            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.removeIf(line -> {
                    String stripped = ChatColor.stripColor(line);
                    return stripped.startsWith("Players killed:") || stripped.startsWith("Last killed:");
                });
                meta.setLore(lore.isEmpty() ? null : lore);
            }

            // Apply changes safely
            new BukkitRunnable() {
                @Override
                public void run() {
                    weapon.setItemMeta(meta);
                }
            }.runTask(this);

            saveKillData();
            player.sendMessage(ChatColor.GREEN + "Kill tracking has been fully reset for this weapon.");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    // Class to store kill data per weapon
    private static class WeaponKillData {
        Set<UUID> uniqueKills = Collections.synchronizedSet(new HashSet<>());
        UUID lastKilled = null;
    }
}
