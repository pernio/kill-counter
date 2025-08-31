package com.jinzo.commands.admin.set;

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

public class lastKilledKill implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return false;
        }

        if (args.length < 3 || args.length > 4) {
            player.sendMessage(Component.text("Usage: /kt set killed {name} [#force]", NamedTextColor.YELLOW));
            return false;
        }

        if (args[2].isEmpty()) {
            player.sendMessage(Component.text("Please provide a valid name.", NamedTextColor.RED));
            return false;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!(args.length > 3 && args[3].equalsIgnoreCase("#force")) && !WeaponUtil.isTrackedWeapon(held)) {
            player.sendMessage(Component.text("You're not holding a valid tracked weapon.", NamedTextColor.RED));
            return false;
        }

        if (args[2].equals("null")) {
            WeaponUtil.clearData(held, WeaponUtil.LAST_KILLED_KEY);
            player.sendMessage(Component.text("Last killed cleared.", NamedTextColor.GREEN));
        } else {
            WeaponUtil.setLastKilled(held, args[2]);
            player.sendMessage(Component.text("Last killed set to " + args[2] + ".", NamedTextColor.GREEN));
        }

        LoreUtil.updateLoreFromNBT(held);
        return true;
    }
}