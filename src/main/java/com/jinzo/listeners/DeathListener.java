package com.jinzo.listeners;

import com.jinzo.KillTracker;
import com.jinzo.utils.ConfigManager;
import com.jinzo.utils.LoreUtil;
import com.jinzo.utils.WeaponUtil;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DeathListener implements Listener {
    private final KillTracker plugin;
    private final Map<UUID, Long> killCooldowns = new HashMap<>();

    public DeathListener(KillTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        if (!dead.hasPermission("killtracker.use")) return;

        ConfigManager config = plugin.getConfiguration();

        if (config.killStreak) {
            PlayerInventory inv = dead.getInventory();

            Consumer<ItemStack> reset = item -> {
                if (item != null && WeaponUtil.isTrackedWeapon(item)) {
                    WeaponUtil.setKillStreak(item, 0);
                    LoreUtil.updateLoreFromNBT(item);
                }
            };

            // main storage
            for (ItemStack item : inv.getContents()) reset.accept(item);

            // armor & extra (offhand lives in extra on some versions)
            for (ItemStack item : inv.getArmorContents()) reset.accept(item);
            for (ItemStack item : inv.getExtraContents()) reset.accept(item);

            // explicit offhand (covers all versions)
            reset.accept(inv.getItemInOffHand());

            // ITEM ON CURSOR (the "dragging" case)
            ItemStack cursor = dead.getItemOnCursor();
            if (cursor != null) {
                reset.accept(cursor);
                // make sure changes stick
                dead.setItemOnCursor(cursor);
            }
        }

        Player killer = dead.getKiller();
        if (killer == null || dead.getUniqueId().equals(killer.getUniqueId())) return;

        processKill(killer, dead.getName(), plugin.getConfiguration());
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();

        if (dead instanceof Player) return;

        Player killer = dead.getKiller();
        if (killer == null) return;

        ConfigManager config = plugin.getConfiguration();
        if (!config.countMobKills) return;

        String killedName = dead.getCustomName() != null
                ? dead.getCustomName()
                : dead.getType().name().toLowerCase().replace("_", " ");

        processKill(killer, killedName, config);
    }

    private void processKill(Player killer, String killedName, ConfigManager config) {
        if (!killer.hasPermission("killtracker.use")) return;

        RegionScheduler scheduler = Bukkit.getRegionScheduler();
        scheduler.execute(plugin, killer.getLocation(), () -> {
            UUID killerId = killer.getUniqueId();
            long now = System.currentTimeMillis();
            long cooldownMillis = config.killCooldown * 1000L;

            synchronized (killCooldowns) {
                Long lastKillTime = killCooldowns.get(killerId);
                if (lastKillTime != null && now - lastKillTime < cooldownMillis) return;
            }

            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (!WeaponUtil.isTrackedWeapon(weapon)) return;

            String lastKilled = WeaponUtil.getLastKilled(weapon);
            String lastKiller = WeaponUtil.getLastKiller(weapon);
            if (!config.countLastKilled && lastKilled != null && lastKilled.equalsIgnoreCase(killedName)) return;
            if (!config.countLastKiller && lastKiller != null && lastKiller.equalsIgnoreCase(killedName)) return;

            int currentKills = WeaponUtil.getKillCount(weapon);
            int maxKills = config.maxKills;
            if (maxKills != -1 && currentKills >= maxKills) return;

            synchronized (killCooldowns) {
                killCooldowns.put(killerId, now);
            }

            // Increment and update
            WeaponUtil.incrementKillCount(weapon);
            WeaponUtil.setLastKilled(weapon, killedName);
            WeaponUtil.setLastKiller(weapon, killer.getName());

            if (config.killStreak) {
                int streak = WeaponUtil.getKillStreak(weapon) + 1;
                WeaponUtil.setKillStreak(weapon, streak);
            }

            LoreUtil.updateLoreFromNBT(weapon);
        });
    }
}
