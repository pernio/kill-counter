package com.jinzo.commands.use;

import com.jinzo.KillTracker;
import com.jinzo.utils.ConfigManager;
import com.jinzo.utils.LoreUtil;
import com.jinzo.utils.WeaponUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class infoKill implements CommandExecutor {

    private final KillTracker plugin = KillTracker.getInstance();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!WeaponUtil.isTrackedWeapon(item)) {
            player.sendMessage(Component.text("You must hold a tracked weapon.").color(NamedTextColor.RED));
            return false;
        }

        int kills = WeaponUtil.getKillCount(item);
        String lastKilled = WeaponUtil.getLastKilled(item);
        int killStreak = WeaponUtil.getKillStreak(item); // returns 0 if not set
        TextColor killsColor = ConfigManager.getColorDataForKillCount(kills).color;
        TextColor killSteakColor = ConfigManager.getColorDataForKillCount(killStreak).color;

        player.sendMessage(Component.text("== Kill Tracker Stats ==", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Kills: ", NamedTextColor.GRAY)
                .append(Component.text(LoreUtil.formatNumber(Math.abs(kills)), killsColor)));

        if (plugin.getConfiguration().killStreak) {
            player.sendMessage(Component.text("Killstreak: ", NamedTextColor.GRAY)
                    .append(Component.text(LoreUtil.formatNumber(Math.abs(killStreak)), killSteakColor)));
        }

        if (lastKilled != null) {
            player.sendMessage(Component.text("Last Killed: ", NamedTextColor.GRAY)
                    .append(Component.text(lastKilled, NamedTextColor.RED)));
        }

        return true;
    }
}
