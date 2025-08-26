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

public class streakKill implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return false;
        }

        if (args.length < 3 || args.length > 4) {
            player.sendMessage(Component.text("Usage: /kt set streak {amount} [#force]", NamedTextColor.YELLOW));
            return false;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Please enter a valid number.", NamedTextColor.RED));
            return false;
        }

        if (amount < 0) {
            player.sendMessage(Component.text("Amount must be equal or higher than 0", NamedTextColor.RED));
            return false;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!(args.length > 3 && args[3].equalsIgnoreCase("#force")) && !WeaponUtil.isTrackedWeapon(held)) {
            player.sendMessage(Component.text("You're not holding a valid tracked weapon.", NamedTextColor.RED));
            return false;
        }

        WeaponUtil.setKillStreak(held, amount);

        player.sendMessage(Component.text("Kill streak set to " + LoreUtil.formatNumber(amount) + ".", NamedTextColor.GREEN));

        LoreUtil.updateLoreFromNBT(held);
        return true;
    }
}