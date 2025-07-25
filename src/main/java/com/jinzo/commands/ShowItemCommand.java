package com.jinzo.commands;

import com.jinzo.data.ConfigManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShowItemCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ShowItemCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        long cooldownMillis = configManager.getCooldown() * 1000L;

        if (cooldowns.containsKey(playerId)) {
            long lastUsed = cooldowns.get(playerId);
            if ((currentTime - lastUsed) < cooldownMillis) {
                long timeLeft = (cooldownMillis - (currentTime - lastUsed)) / 1000;
                player.sendMessage(Component.text("Please wait " + timeLeft + " more seconds before using this command again.", NamedTextColor.RED));
                return true;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("You must hold an item.");
            return true;
        }

        cooldowns.put(playerId, currentTime);

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
