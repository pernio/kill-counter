package com.jinzo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShowItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("You must hold an item.");
            return true;
        }

        // Build message using full item hover
        Component message = Component.empty()
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" is holding "))
                .append(Component.text("[" + getDisplayName(item) + "]", NamedTextColor.GREEN)
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
