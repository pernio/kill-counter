package com.jinzo.listeners;

import com.jinzo.KillTracker;
import com.jinzo.utils.ConfigManager;
import com.jinzo.utils.LoreUtil;
import com.jinzo.utils.WeaponUtil;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathListener implements Listener {
    private final KillTracker plugin;
    private final Map<UUID, Long> killCooldowns = new HashMap<>();

    public DeathListener(KillTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() == null) return;
        Player dead = event.getEntity();
        if (!dead.hasPermission("killtracker.use")) return;
        Player killer = dead.getKiller();
        ConfigManager config = plugin.getConfiguration();

        // Reset kill streak if enabled
        if (config.killStreak) {
            for (ItemStack item : dead.getInventory().getContents()) {
                if (item != null && WeaponUtil.isTrackedWeapon(item)) {
                    WeaponUtil.setKillStreak(item, 0);
                    LoreUtil.updateLoreFromNBT(item);
                }
            }
        }

        if (killer == null || dead.getUniqueId().equals(killer.getUniqueId())) return;

        processKill(killer, dead.getName(), config);
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();

        // Ignore players here — handled in onPlayerDeath
        if (dead instanceof Player) return;

        Player killer = dead.getKiller(); // valid now
        if (killer == null) return;

        ConfigManager config = plugin.getConfiguration();
        if (!config.countMobKills) return;

        // Use custom name if present, otherwise entity type name
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

            int previousLevel = config.getKillLevel(currentKills);

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

            // Notify if level changed
            if (config.notifyOnLevelUp) {
                int newKills = currentKills + 1;
                int newLevel = config.getKillLevel(newKills);
                TextColor color = ConfigManager.getColorDataForKillCount(newKills).color;
                if (newLevel > previousLevel) {
                    ConfigManager.ColorData colorData = ConfigManager.getColorDataForKillCount(newKills);
                    killer.sendMessage(Component.text(
                            "Your weapon has reached ", NamedTextColor.GRAY)
                            .append(Component.text(LoreUtil.formatNumber(newKills), color))
                            .append(Component.text(newKills == 1 ? " kill " : " kills " + "and leveled up to ", NamedTextColor.GRAY))
                            .append(Component.text(colorData.name).color(colorData.color))
                    );
                }
            }
        });
    }
}
