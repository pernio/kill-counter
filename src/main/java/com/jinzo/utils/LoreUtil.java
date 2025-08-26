package com.jinzo.utils;

import com.jinzo.KillTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreUtil {
    public static void updateLoreFromNBT(ItemStack item) {
        KillTracker plugin = KillTracker.getInstance();
        if (item == null || item.getType().isAir()) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = new ArrayList<>();

        int count = WeaponUtil.getKillCount(item);
        String last = WeaponUtil.getLastKilled(item);

        if (count > 0) {
            TextColor color = ConfigManager.getColorDataForKillCount(count).color;
            String formatted = formatNumber(count);

            lore.add(Component.text("Amount killed: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(formatted)
                            .color(color)
                            .decoration(TextDecoration.ITALIC, false)));
        }

        if (last != null) {
            lore.add(Component.text("Last killed: " + last)
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        int streak = WeaponUtil.getKillStreak(item);
        if (plugin.getConfiguration().killStreak && streak > 0) {
            TextColor color = ConfigManager.getColorDataForKillCount(streak).color;
            String formatted = formatNumber(streak);

            lore.add(Component.text("Kill streak: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(formatted)
                            .color(color)
                            .decoration(TextDecoration.ITALIC, false)));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public static String formatNumber(int number) {
        if (number >= 1_000_000_000) {
            return formatSuffix(number, 1_000_000_000, "b");
        } else if (number >= 1_000_000) {
            return formatSuffix(number, 1_000_000, "m");
        } else if (number >= 1_000) {
            return formatSuffix(number, 1_000, "k");
        }
        return String.valueOf(number);
    }

    private static String formatSuffix(int number, int divider, String suffix) {
        double result = number / (double) divider;
        if (result % 1 == 0) {
            return String.format("%.0f%s", result, suffix);
        } else {
            return String.format("%.1f%s", result, suffix);
        }
    }
}
