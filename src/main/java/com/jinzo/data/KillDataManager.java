package com.jinzo.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jinzo.KillCounter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillDataManager {
    private final KillCounter plugin;
    private final File dataFile;
    private final Gson gson = new Gson();
    private final Map<String, WeaponKillData> killData = new ConcurrentHashMap<>();

    public KillDataManager(KillCounter plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "killData.json");
    }

    public void loadKillData() {
        try {
            if (!dataFile.exists()) return;
            FileReader reader = new FileReader(dataFile);
            Type type = new TypeToken<Map<String, WeaponKillData>>(){}.getType();
            Map<String, WeaponKillData> loaded = gson.fromJson(reader, type);
            if (loaded != null) killData.putAll(loaded);
            reader.close();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load killData.json");
            e.printStackTrace();
        }
    }

    public void saveKillData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(killData, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save killData.json");
            e.printStackTrace();
        }
    }

    public Map<String, WeaponKillData> getKillDataMap() {
        return killData;
    }
}
