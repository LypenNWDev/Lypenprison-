package com.lypen.prison;

import com.lypen.prison.command.PrisonCommand;
import com.lypen.prison.command.UnprisonCommand;
import com.lypen.prison.listener.PlayerEvents;
import com.lypen.prison.manager.BanManager;
import com.lypen.prison.manager.PrisonManager;
import com.lypen.prison.manager.PrisonLogManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class LypenPrison extends JavaPlugin {

    private BanManager banManager;
    private PrisonManager prisonManager;
    private PrisonLogManager logManager;
    private World prisonWorld;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        banManager = new BanManager(this);
        logManager = new PrisonLogManager(this);
        
        // Setup void world
        setupPrisonWorld();
        
        prisonManager = new PrisonManager(this);

        getCommand("prison").setExecutor(new PrisonCommand(this));
        getCommand("unprison").setExecutor(new UnprisonCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);

        getLogger().info("LypenPrison aktif edildi.");
    }

    @Override
    public void onDisable() {
        getLogger().info("LypenPrison devre disi.");
    }

    private void setupPrisonWorld() {
        WorldCreator creator = new WorldCreator("plugins/LypenPrison/prison_world");
        creator.generator(new VoidGenerator());
        prisonWorld = creator.createWorld();
        if (prisonWorld != null) {
            prisonWorld.setSpawnLocation(0, 100, 0);
        } else {
            getLogger().severe("Prison dünyasi olusturulamadi!");
        }
    }

    public String reloadAll() {
        java.io.File configFile = new java.io.File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        try {
            org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
            config.load(configFile);
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            return "&cconfig.yml dosyasında YML yazım/format hatası var!";
        } catch (Exception e) {
            return "&cconfig.yml okunurken hata oluştu!";
        }

        String banRes = banManager.reload();
        if (banRes != null) return banRes;

        String logRes = logManager.reload();
        if (logRes != null) return logRes;

        reloadConfig();
        return "&aLypenPrison (Config, Bans, Logs) başarıyla yenilendi!";
    }

    public BanManager getBanManager() { return banManager; }
    public PrisonManager getPrisonManager() { return prisonManager; }
    public PrisonLogManager getLogManager() { return logManager; }
    public World getPrisonWorld() { return prisonWorld; }

    // Simple Void Generator
    public static class VoidGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world); // returns empty chunk
        }
    }
}
