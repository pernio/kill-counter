package com.jinzo.commands.use;

import com.jinzo.KillTracker;
import com.jinzo.utils.ConfigManager;
import com.jinzo.utils.WeaponUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class showKill implements CommandExecutor {
    private final ConfigManager configManager = KillTracker.getInstance().getConfiguration();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        long cooldownMillis = configManager.showCooldown * 1000L;

        if (cooldowns.containsKey(playerId)) {
            long lastUsed = cooldowns.get(playerId);
            if ((currentTime - lastUsed) < cooldownMillis) {
                long timeLeft = (cooldownMillis - (currentTime - lastUsed)) / 1000;
                player.sendMessage(Component.text("Please wait " + timeLeft + " more seconds before using this command again.", NamedTextColor.RED));
                return false;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("You must hold an item.");
            return false;
        }

        cooldowns.put(playerId, currentTime);
        TextColor color = NamedTextColor.GREEN;

        if (WeaponUtil.isTrackedWeapon(item)) {
            // Overwrite color by kill color if weapon is tracked
            int kills = WeaponUtil.getKillCount(item);
            color = ConfigManager.getColorDataForKillCount(kills).color;
        }

        Component message = Component.empty()
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" is holding "))
                .append(Component.text("[" + getDisplayName(item) + "]", color)
                        .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value())));

        Bukkit.broadcast(message);
        return true;
    }

    private String getDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace('_', ' ').toLowerCase();
    }
}
