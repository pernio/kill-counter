package com.jinzo;

import com.jinzo.commands.KillCommandExecutor;
import com.jinzo.commands.ShowItemCommand;
import com.jinzo.data.ConfigManager;
import com.jinzo.data.KillDataManager;
import com.jinzo.listeners.DeathListener;
import com.jinzo.listeners.ItemBreakListener;
import org.bukkit.plugin.java.JavaPlugin;

public class KillCounter extends JavaPlugin {

    private KillDataManager killDataManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        this.killDataManager = new KillDataManager(this);
        killDataManager.loadKillData();

        getServer().getPluginManager().registerEvents(new DeathListener(this, killDataManager), this);
        getServer().getPluginManager().registerEvents(new ItemBreakListener(killDataManager), this);

        var executor = new KillCommandExecutor(this, killDataManager);
        getCommand("lastKilled").setExecutor(executor);
        getCommand("resetKills").setExecutor(executor);
        getCommand("showitem").setExecutor(new ShowItemCommand(configManager));
    }

    @Override
    public void onDisable() {
        killDataManager.saveKillData();
    }
}
