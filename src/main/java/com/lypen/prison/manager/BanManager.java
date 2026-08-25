package com.lypen.prison.manager;

import com.lypen.prison.LypenPrison;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BanManager {

    private final LypenPrison plugin;
    private File file;
    private FileConfiguration config;

    public BanManager(LypenPrison plugin) {
        this.plugin = plugin;
        reload();
    }

    public String reload() {
        file = new File(plugin.getDataFolder(), "bans.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return "&cbans.yml dosyası oluşturulamadı!";
            }
        }
        try {
            config = new YamlConfiguration();
            config.load(file);
            return null;
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            return "&cbans.yml dosyasında YML yazım/format hatası var!";
        } catch (Exception e) {
            return "&cbans.yml okunurken bilinmeyen bir hata oluştu!";
        }
    }

    public void banPlayer(String uuid, String name, String ip) {
        String path = "bans." + uuid;
        config.set(path + ".name", name);
        config.set(path + ".ip", ip);
        config.set(path + ".date", System.currentTimeMillis());
        saveConfig();
    }

    public boolean isBannedByUUID(String uuid) {
        return config.contains("bans." + uuid);
    }

    public boolean isBannedByIP(String ip) {
        if (config.getConfigurationSection("bans") == null) return false;
        for (String uuid : config.getConfigurationSection("bans").getKeys(false)) {
            if (ip.equals(config.getString("bans." + uuid + ".ip"))) {
                return true;
            }
        }
        return false;
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
