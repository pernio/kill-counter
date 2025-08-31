package com.jinzo.commands.admin;

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

public class addKill implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return false;
        }

        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /kt add {amount}", NamedTextColor.YELLOW));
            return false;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Please enter a valid number.", NamedTextColor.RED));
            return false;
        }

        if (amount == 0) {
            player.sendMessage(Component.text("Amount cannot be zero.", NamedTextColor.RED));
            return false;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!WeaponUtil.isTrackedWeapon(held)) {
            player.sendMessage(Component.text("You're not holding a valid tracked weapon.", NamedTextColor.RED));
            return false;
        }

        String action = (amount > 0) ? "Added" : "Subtracted";
        int currentKills = WeaponUtil.getKillCount(held);

        if (currentKills == 0 && amount < 0) {
            player.sendMessage(Component.text("Your weapon has no kills to subtract.", NamedTextColor.RED));
            return false;
        }

        int newKills = Math.max(currentKills + amount, 0);
        int affectedAmount = newKills - currentKills;

        WeaponUtil.setKillCount(held, newKills);
        if (KillTracker.getInstance().getConfiguration().adminChangeKillStreak) {
            WeaponUtil.setKillStreak(held, newKills);
        }

        player.sendMessage(Component.text(
                action + " " + LoreUtil.formatNumber(Math.abs(affectedAmount)) +
                        (Math.abs(affectedAmount) == 1 ? " kill " : " kills ") +
                        (amount > 0 ? "to" : "from") + " your weapon.",
                NamedTextColor.GREEN));

        LoreUtil.updateLoreFromNBT(held);

        return true;
    }
}
