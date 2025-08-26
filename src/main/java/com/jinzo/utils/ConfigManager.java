package com.jinzo.utils;

import com.jinzo.KillTracker;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final KillTracker plugin;

    public boolean adminChangeKillStreak = false;
    public boolean countMobKills = false;
    public int killCooldown = 120;
    public int showCooldown = 60;
    public int maxKills = -1; // -1 means no limit
    public boolean killStreak = true;
    public boolean notifyOnLevelUp = true;
    public Set<Material> trackedWeapons = new HashSet<>();
    public boolean countLastKilled = false;
    public boolean countLastKiller = false;


    public record KillLevel(int kills, String name, TextColor color) {}
    private static final List<KillLevel> killLevels = new ArrayList<>();

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
        notifyOnLevelUp = cfg.getBoolean("notify_on_level_up", true);
        countLastKilled = cfg.getBoolean("count_last_killed", false);
        countLastKiller = cfg.getBoolean("count_last_killer", false);

        trackedWeapons.clear();
        killLevels.clear();

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

        // Load kill levels
        List<Map<?, ?>> levels = cfg.getMapList("kill_levels");
        for (Map<?, ?> entry : levels) {
            Object killsObj = entry.get("kills");
            Object nameObj = entry.get("name");
            Object colorObj = entry.get("color");

            if (killsObj instanceof Number && colorObj instanceof String colorStr) {
                int kills = ((Number) killsObj).intValue();
                String name = nameObj instanceof String ? (String) nameObj : "Level " + kills;
                NamedTextColor color = parseNamedTextColor(colorStr.trim().toLowerCase(Locale.ROOT));
                if (color != null) {
                    killLevels.add(new KillLevel(kills, name, color));
                } else {
                    plugin.getLogger().warning("Invalid NamedTextColor: " + colorStr);
                }
            }
        }

        // Sort kill levels ascending
        killLevels.sort(Comparator.comparingInt(KillLevel::kills));
    }

    public static class ColorData {
        public TextColor color;
        public String name;

        public ColorData(TextColor color, String name) {
            this.color = color;
            this.name = name;
        }
    }

    public static ColorData getColorDataForKillCount(int count) {
        ColorData lastColor = new ColorData(NamedTextColor.GRAY, null);
        for (KillLevel level : killLevels) {
            if (count >= level.kills()) {
                lastColor.color = level.color();
                lastColor.name = level.name();
            } else {
                break;
            }
        }
        return lastColor;
    }

    private NamedTextColor parseNamedTextColor(String colorName) {
        try {
            return NamedTextColor.NAMES.value(colorName); // Adventure color resolver
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public int getKillLevel(int kills) {
        int level = 0;
        for (KillLevel entry : killLevels) {
            if (kills >= entry.kills()) {
                level++;
            } else {
                break;
            }
        }
        return level;
    }
}
