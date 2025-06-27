package com.jinzo.commands;

import com.jinzo.KillCounter;
import com.jinzo.data.KillDataManager;
import com.jinzo.data.WeaponKillData;
import com.jinzo.utils.WeaponUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

public class KillCommandExecutor implements CommandExecutor {
    private final KillCounter plugin;
    private final KillDataManager killDataManager;

    public KillCommandExecutor(KillCounter plugin, KillDataManager manager) {
        this.plugin = plugin;
        this.killDataManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!WeaponUtil.isTrackedWeapon(weapon)) {
            player.sendMessage(ChatColor.RED + "You must hold a tracked weapon.");
            return true;
        }

        String key = WeaponUtil.getOrCreateWeaponKey(weapon, plugin);
        Map<String, WeaponKillData> map = killDataManager.getKillDataMap();

        if (command.getName().equalsIgnoreCase("lastKilled")) {
            if (!map.containsKey(key)) {
                player.sendMessage(ChatColor.YELLOW + "This weapon has no kill data.");
                return true;
            }

            WeaponKillData data = map.get(key);
            if (data.lastKilled == null) {
                player.sendMessage(ChatColor.YELLOW + "No kills recorded yet for this weapon.");
            } else {
                String name = Bukkit.getOfflinePlayer(data.lastKilled).getName();
                player.sendMessage(ChatColor.GREEN + "Last player killed: " + ChatColor.GOLD + name);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("resetKills")) {
            map.remove(key);
            WeaponUtil.resetWeaponMeta(weapon, plugin);
            killDataManager.saveKillData();
            player.sendMessage(ChatColor.GREEN + "Kill data reset.");
            return true;
        }

        return false;
    }
}
