package com.jinzo.commands.use;

import com.jinzo.KillTracker;
import com.jinzo.utils.WeaponUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class resetKill implements CommandExecutor {
    private final KillTracker plugin = KillTracker.getInstance();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return false;
        }

        WeaponUtil.resetWeaponMeta(player.getInventory().getItemInMainHand(), plugin);
        player.sendMessage(Component.text("Kill data reset.", NamedTextColor.GREEN));
        return true;
    }
}
