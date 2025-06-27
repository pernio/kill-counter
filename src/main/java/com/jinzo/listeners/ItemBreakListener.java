package com.jinzo.listeners;

import com.jinzo.data.KillDataManager;
import com.jinzo.utils.WeaponUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ItemBreakListener implements Listener {
    private final KillDataManager killDataManager;

    public ItemBreakListener(KillDataManager manager) {
        this.killDataManager = manager;
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        ItemStack brokenItem = event.getBrokenItem();
        if (!WeaponUtil.isTrackedWeapon(brokenItem)) return;
        String key = WeaponUtil.getWeaponKey(brokenItem);
        killDataManager.getKillDataMap().remove(key);
        killDataManager.saveKillData();
    }
}
