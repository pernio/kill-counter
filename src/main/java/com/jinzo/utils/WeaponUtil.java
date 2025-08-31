package com.jinzo.utils;

import com.jinzo.KillTracker;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WeaponUtil {
    public static NamespacedKey KILL_COUNT_KEY;
    public static NamespacedKey LAST_KILLED_KEY;
    public static NamespacedKey LAST_KILLER_KEY;
    public static NamespacedKey KILL_STREAK_KEY;

    public static void initialize(KillTracker plugin) {
        KILL_COUNT_KEY = new NamespacedKey(plugin, "kill-count");
        LAST_KILLED_KEY = new NamespacedKey(plugin, "last-killed");
        LAST_KILLER_KEY = new NamespacedKey(plugin, "last-killer");
        KILL_STREAK_KEY = new NamespacedKey(plugin, "kill-streak");
    }

    public static boolean isTrackedWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return KillTracker.getInstance().getConfiguration().trackedWeapons.contains(item.getType());
    }

    public static int getKillCount(ItemStack weapon) {
        if (!weapon.hasItemMeta()) return 0;
        Integer count = weapon.getItemMeta().getPersistentDataContainer().get(KILL_COUNT_KEY, PersistentDataType.INTEGER);
        return count != null ? count : 0;
    }

    public static void setKillCount(ItemStack weapon, int count) {
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(KILL_COUNT_KEY, PersistentDataType.INTEGER, count);
        weapon.setItemMeta(meta);
    }

    public static void incrementKillCount(ItemStack weapon) {
        int current = getKillCount(weapon);
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(KILL_COUNT_KEY, PersistentDataType.INTEGER, current + 1);
        weapon.setItemMeta(meta);
    }

    public static String getLastKilled(ItemStack weapon) {
        if (!weapon.hasItemMeta()) return null;
        return weapon.getItemMeta().getPersistentDataContainer().get(LAST_KILLED_KEY, PersistentDataType.STRING);
    }

    public static void setLastKilled(ItemStack weapon, String name) {
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(LAST_KILLED_KEY, PersistentDataType.STRING, name);
        weapon.setItemMeta(meta);
    }

    public static String getLastKiller(ItemStack weapon) {
        if (!weapon.hasItemMeta()) return null;
        return weapon.getItemMeta().getPersistentDataContainer().get(LAST_KILLER_KEY, PersistentDataType.STRING);
    }

    public static void setLastKiller(ItemStack weapon, String name) {
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(LAST_KILLER_KEY, PersistentDataType.STRING, name);
        weapon.setItemMeta(meta);
    }

    public static int getKillStreak(ItemStack weapon) {
        if (!weapon.hasItemMeta()) return 0;
        Integer streak = weapon.getItemMeta().getPersistentDataContainer().get(KILL_STREAK_KEY, PersistentDataType.INTEGER);
        return streak != null ? streak : 0;
    }

    public static void setKillStreak(ItemStack weapon, int streak) {
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(KILL_STREAK_KEY, PersistentDataType.INTEGER, streak);
        weapon.setItemMeta(meta);
    }

    public static void resetWeaponMeta(ItemStack weapon) {
        ItemMeta meta = weapon.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(KILL_COUNT_KEY);
        container.remove(LAST_KILLED_KEY);
        container.remove(LAST_KILLER_KEY);
        container.remove(KILL_STREAK_KEY);
        meta.setLore(null);
        weapon.setItemMeta(meta);
    }

    public static void clearData(ItemStack weapon, NamespacedKey key) {
        if (weapon == null || !weapon.hasItemMeta()) return;
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().remove(key);
        weapon.setItemMeta(meta);
    }
}
