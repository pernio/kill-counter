package com.jinzo.commands.admin;

import com.jinzo.KillTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class reloadKill implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof org.bukkit.entity.Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return false;
        }

        KillTracker plugin = KillTracker.getInstance();
        plugin.reloadConfig();
        plugin.getConfiguration().reload();

        player.sendMessage(Component.text("Kill Tracker configuration reloaded.", NamedTextColor.GREEN));
        return true;
    }
}
