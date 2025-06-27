package com.jinzo.listeners;

import com.jinzo.KillCounter;
import com.jinzo.data.KillDataManager;
import com.jinzo.data.WeaponKillData;
import com.jinzo.utils.LoreUtil;
import com.jinzo.utils.WeaponUtil;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathListener implements Listener {
    private final KillCounter plugin;
    private final KillDataManager killDataManager;

    public DeathListener(KillCounter plugin, KillDataManager manager) {
        this.plugin = plugin;
        this.killDataManager = manager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;

        RegionScheduler scheduler = Bukkit.getRegionScheduler();
        scheduler.execute(plugin, killer.getLocation(), () -> {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (!WeaponUtil.isTrackedWeapon(weapon)) return;

            String key = WeaponUtil.getOrCreateWeaponKey(weapon, plugin);
            WeaponKillData data = killDataManager.getKillDataMap().getOrDefault(key, new WeaponKillData());
            data.uniqueKills.add(dead.getUniqueId());
            data.lastKilled = dead.getUniqueId();
            killDataManager.getKillDataMap().put(key, data);

            new BukkitRunnable() {
                @Override
                public void run() {
                    LoreUtil.updateLore(weapon, data);
                    killDataManager.saveKillData();
                }
            }.runTask(plugin);
        });
    }
}
