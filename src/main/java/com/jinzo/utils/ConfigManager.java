package com.jinzo.utils;

import com.jinzo.KillTracker;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final KillTracker plugin;

    public boolean adminChangeKillStreak = false;
    public boolean countMobKills = false;
    public int killCooldown = 120;
    public int showCooldown = 60;
    public int maxKills = -1;
    public boolean killStreak = true;
    public Set<Material> trackedWeapons = new HashSet<>();
    public boolean countLastKilled = false;
    public boolean countLastKiller = false;
    public boolean onlyShowTrackedWeapons = false;
    public String showItemText = "shared";

    public ConfigManager(KillTracker plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        adminChangeKillStreak = cfg.getBoolean("admin_change_kill_streak", false);
        countMobKills = cfg.getBoolean("count_mob_kills", false);
        killCooldown = cfg.getInt("kill_cooldown", 120);
        showCooldown = cfg.getInt("show_cooldown", 60);
        maxKills = cfg.getInt("max_kills", -1);
        killStreak = cfg.getBoolean("kill_streak", true);
        countLastKilled = cfg.getBoolean("count_last_killed", false);
        countLastKiller = cfg.getBoolean("count_last_killer", false);
        onlyShowTrackedWeapons = cfg.getBoolean("only_show_tracked_weapons", false);
        showItemText = cfg.getString("show_item_text", "shared");

        trackedWeapons.clear();

        // Load tracked weapons
        List<String> weaponsList = cfg.getStringList("tracked_weapons");
        for (String weaponName : weaponsList) {
            try {
                Material material = Material.valueOf(weaponName.toUpperCase(Locale.ROOT));
                trackedWeapons.add(material);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid material in tracked_weapons: " + weaponName);
            }
        }
    }
}
