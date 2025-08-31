package com.jinzo.commands.use;

import com.jinzo.KillTracker;
import com.jinzo.utils.LoreUtil;
import com.jinzo.utils.WeaponUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class infoKill implements CommandExecutor {

    private final KillTracker plugin = KillTracker.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        int kills = WeaponUtil.getKillCount(item);
        String lastKilled = WeaponUtil.getLastKilled(item);
        int killStreak = WeaponUtil.getKillStreak(item);

        player.sendMessage(Component.text("== Kill Tracker Stats ==", NamedTextColor.GOLD));

        player.sendMessage(Component.text("Kills: ", NamedTextColor.GRAY)
                .append(
                        Component.text(LoreUtil.formatNumber(Math.abs(kills)))
                                .hoverEvent(Component.text(String.valueOf(kills)))
                ));

        if (plugin.getConfiguration().killStreak) {
            player.sendMessage(Component.text("Killstreak: ", NamedTextColor.GRAY)
                    .append(
                            Component.text(LoreUtil.formatNumber(Math.abs(killStreak)))
                                    .hoverEvent(Component.text(String.valueOf(killStreak)))
                    ));
        }

        if (lastKilled != null) {
            player.sendMessage(Component.text("Last Killed: ", NamedTextColor.GRAY)
                    .append(Component.text(lastKilled)));
        }

        return true;
    }
}
