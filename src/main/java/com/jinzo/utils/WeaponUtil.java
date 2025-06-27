package com.jinzo.utils;

import com.jinzo.KillCounter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class WeaponUtil {
    private static final Set<Material> TRACKED_ITEMS = Set.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.BOW, Material.CROSSBOW, Material.MACE, Material.TRIDENT
    );

    public static boolean isTrackedWeapon(ItemStack item) {
        return item != null && TRACKED_ITEMS.contains(item.getType());
    }

    public static String getWeaponKey(ItemStack weapon) {
        if (weapon == null || !weapon.hasItemMeta()) return "";
        ItemMeta meta = weapon.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.get(new NamespacedKey("killcounter", "weapon-uuid"), PersistentDataType.STRING);
    }

    public static String getOrCreateWeaponKey(ItemStack weapon, KillCounter plugin) {
        ItemMeta meta = weapon.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "weapon-uuid");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String id = container.get(key, PersistentDataType.STRING);
        if (id == null) {
            id = UUID.randomUUID().toString();
            container.set(key, PersistentDataType.STRING, id);
            String finalId = id;
            new BukkitRunnable() {
                @Override
                public void run() {
                    weapon.setItemMeta(meta);
                }
            }.runTask(plugin);
        }
        return id;
    }

    public static void resetWeaponMeta(ItemStack weapon, KillCounter plugin) {
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "weapon-uuid"));
        if (meta.hasLore()) {
            meta.setLore(null);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                weapon.setItemMeta(meta);
            }
        }.runTask(plugin);
    }
}

