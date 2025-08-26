package com.jinzo.listeners;

import com.jinzo.KillTracker;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final KillTracker plugin;

    public JoinListener(KillTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && plugin.isUpdateAvailable()) {
            event.getPlayer().sendMessage("§e[Kill Tracker] §cA new version is available! §7(" +
                    plugin.getCurrentVersion() + " → " + plugin.getLatestVersionName() + ")\n" +
                    "§aDownload here: §b" + plugin.getDownloadURL());
        }
    }
}
