package com.jinzo.utils;

import com.jinzo.data.WeaponKillData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreUtil {
    public static void updateLore(ItemStack weapon, WeaponKillData data) {
        ItemMeta meta = weapon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Players killed:") || ChatColor.stripColor(line).startsWith("Last killed:"));
        lore.add(ChatColor.RED + "Players killed: " + data.uniqueKills.size());
        lore.add(ChatColor.GOLD + "Last killed: " + Bukkit.getOfflinePlayer(data.lastKilled).getName());
        meta.setLore(lore);
        weapon.setItemMeta(meta);
    }
}

