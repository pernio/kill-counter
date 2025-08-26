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

public class lastKill implements CommandExecutor {
    private final KillTracker plugin = KillTracker.getInstance();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return false;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!WeaponUtil.isTrackedWeapon(weapon)) {
            player.sendMessage(Component.text("You must hold a tracked weapon.", NamedTextColor.RED));
            return false;
        }

        String lastKilled = WeaponUtil.getLastKilled(weapon);
        if (lastKilled == null) {
            player.sendMessage(Component.text("No kills recorded yet for this weapon.", NamedTextColor.YELLOW));
            return false;
        } else {
            player.sendMessage(
                    Component.text("Last killed: ", NamedTextColor.GRAY)
                            .append(Component.text(lastKilled, NamedTextColor.RED))
            );
        }
        return true;
    }
}
