package com.jinzo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jinzo.commands.*;
import com.jinzo.listeners.JoinListener;
import com.jinzo.utils.ConfigManager;
import com.jinzo.listeners.DeathListener;
import com.jinzo.listeners.LoreUpdateListener;
import com.jinzo.utils.WeaponUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class KillTracker extends JavaPlugin {
    private static KillTracker instance;
    private ConfigManager configuration;

    private static final String MODRINTH_PROJECT_ID = "kill-tracker";
    private static String CURRENT_VERSION = null;

    private String latestVersionName = null;
    private boolean updateAvailable = false;
    private String downloadURL = null;

    @Override
    public void onEnable() {
        instance = this;

        checkForUpdates();
        configuration = new ConfigManager(this);

        saveDefaultConfig();
        WeaponUtil.initialize(this);

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new LoreUpdateListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        killCommand killCommand = new killCommand();

        PluginCommand command = getCommand("killtracker");
        assert command != null;
        command.setExecutor(killCommand);
        command.setTabCompleter(killCommand);

        getLogger().info("Kill Tracker has been enabled!");
        CURRENT_VERSION = getDescription().getVersion();
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("Kill Tracker has been disabled!");
    }

    public static KillTracker getInstance() {
        return instance;
    }

    public ConfigManager getConfiguration() { return this.configuration; }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersionName() {
        return latestVersionName;
    }

    public String getCurrentVersion() {
        return CURRENT_VERSION;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    private void checkForUpdates() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT_ID + "/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                     Scanner scanner = new Scanner(reader)) {
                    StringBuilder jsonText = new StringBuilder();
                    while (scanner.hasNext()) {
                        jsonText.append(scanner.nextLine());
                    }

                    JsonArray versions = JsonParser.parseString(jsonText.toString()).getAsJsonArray();
                    if (versions.isEmpty()) {
                        getLogger().info("Could not find any versions on Modrinth.");
                        return;
                    }

                    JsonObject latestVersion = versions.get(0).getAsJsonObject();
                    String latestVersionName = latestVersion.get("version_number").getAsString();

                    if (!CURRENT_VERSION.equalsIgnoreCase(latestVersionName)) {
                        this.latestVersionName = latestVersionName;
                        this.updateAvailable = true;
                        this.downloadURL = "https://modrinth.com/plugin/" + MODRINTH_PROJECT_ID + "/version/" + latestVersion.get("id").getAsString();

                        String reset = "\u001B[0m";
                        String grey = "\u001B[37m";
                        String red = "\u001B[31m";
                        String green = "\u001B[32m";
                        String yellow = "\u001B[33m";

                        getLogger().info("\n\n" +
                                grey + "------------------------------------------" + reset + "\n" +
                                grey + "A new version of " + yellow + "Kill Tracker" + grey + " is available!" + reset + "\n" +
                                grey + "New Version: " + red + CURRENT_VERSION + reset +
                                grey + " -> " +
                                green + latestVersionName + reset + "\n" +
                                "Download here: " + downloadURL + "\n" +
                                grey + "------------------------------------------" + reset + "\n"
                        );
                    }
                }

            } catch (Exception e) {
                getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        }, "KillTracker-UpdateChecker").start();
    }
}
