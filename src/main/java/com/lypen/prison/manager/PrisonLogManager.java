package com.lypen.prison.manager;

import com.lypen.prison.LypenPrison;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PrisonLogManager {
    private final LypenPrison plugin;
    private File file;
    private FileConfiguration config;

    public PrisonLogManager(LypenPrison plugin) {
        this.plugin = plugin;
        reload();
    }

    public String reload() {
        file = new File(plugin.getDataFolder(), "logs.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return "&clogs.yml dosyası oluşturulamadı!";
            }
        }
        try {
            config = new YamlConfiguration();
            config.load(file);
            return null;
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            return "&clogs.yml dosyasında YML yazım/format hatası var!";
        } catch (Exception e) {
            return "&clogs.yml okunurken bilinmeyen bir hata oluştu!";
        }
    }

    public void addLog(String targetName, String adminName, String type) {
        String path = "logs." + targetName.toLowerCase();
        List<String> history = config.getStringList(path);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        history.add("&eYetkili: &f" + adminName + " &8| &eTür: &f" + type + " &8| &eTarih: &f" + sdf.format(new java.util.Date()));
        config.set(path, history);
        saveConfig();
    }

    public List<String> getLogs(String targetName) {
        return config.getStringList("logs." + targetName.toLowerCase());
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
