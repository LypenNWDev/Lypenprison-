package com.lypen.prison.manager;

import com.lypen.prison.LypenPrison;
import com.lypen.prison.model.PrisonSession;
import com.lypen.prison.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrisonManager {
    private final LypenPrison plugin;
    private final Map<UUID, PrisonSession> activeSessions = new HashMap<>();

    public PrisonManager(LypenPrison plugin) {
        this.plugin = plugin;
        startTimerTask();
    }

    public PrisonSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }
    
    public PrisonSession getSessionByAdmin(UUID adminId) {
        for (PrisonSession session : activeSessions.values()) {
            if (session.getAdminPlayer() != null && session.getAdminPlayer().equals(adminId)) {
                return session;
            }
        }
        return null;
    }

    public boolean isPlayerInPrison(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }
    
    public boolean isAdminInPrison(UUID adminId) {
        return getSessionByAdmin(adminId) != null;
    }

    public void putInPrison(Player target, Player admin, String prisonType, int durationSeconds) {
        // Read config for this type
        String path = "prison_types." + prisonType;
        if (!plugin.getConfig().contains(path)) {
            if (admin != null) admin.sendMessage(ColorUtils.format("&cGeçersiz prison türü: " + prisonType));
            return;
        }

        boolean applyBlindness = plugin.getConfig().getBoolean(path + ".apply_blindness", false);
        boolean canTakeDamage = plugin.getConfig().getBoolean(path + ".can_take_damage", false);
        boolean canMove = plugin.getConfig().getBoolean(path + ".can_move", true);
        boolean canChat = plugin.getConfig().getBoolean(path + ".can_chat", false);
        String title = plugin.getConfig().getString(path + ".title", "&cKontrole Alındın");
        String subtitle = plugin.getConfig().getString(path + ".subtitle", "&eÇıkarsan Ban Yiyeceksin");
        String actionbar = plugin.getConfig().getString(path + ".actionbar", "&bKalan Süre: %time%");

        Location targetPrev = target.getLocation();
        Location adminPrev = admin != null ? admin.getLocation() : null;

        World prisonWorld = plugin.getPrisonWorld();
        int cageIndex = plugin.getConfig().getInt("next_cage_index", 0);
        Location cageCenter = new Location(prisonWorld, cageIndex * 15000.0, 100.0, 0.0);
        
        // Update index for next time
        plugin.getConfig().set("next_cage_index", cageIndex + 1);
        plugin.saveConfig();

        PrisonSession session = new PrisonSession(target.getUniqueId(), admin != null ? admin.getUniqueId() : null, 
                prisonType, durationSeconds, targetPrev, adminPrev, cageCenter,
                applyBlindness, canTakeDamage, canMove, canChat, title, subtitle, actionbar);

        buildCage(cageCenter);

        activeSessions.put(target.getUniqueId(), session);
        
        plugin.getLogManager().addLog(target.getName(), admin != null ? admin.getName() : "Konsol", prisonType);

        // Teleport
        Location targetTp = cageCenter.clone().add(0.5, 1, 0.5);
        targetTp.setYaw(targetPrev.getYaw());
        targetTp.setPitch(targetPrev.getPitch());
        target.teleport(targetTp);

        if (admin != null) {
            Location adminTp = cageCenter.clone().add(3.5, 2, 3.5); // Outside the 5x5 cage
            adminTp.setYaw(135);
            admin.teleport(adminTp);
            admin.setAllowFlight(true);
            admin.setFlying(true);
        }

        // Apply effects
        if (applyBlindness) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
        }
    }

    public void removeFromServer(UUID targetId, boolean ban, String reason) {
        PrisonSession session = activeSessions.remove(targetId);
        if (session == null) return;
        
        Player target = Bukkit.getPlayer(targetId);
        if (target != null && target.isOnline()) {
            if (ban) {
                // Ban is handled by BanManager and kick
                target.kickPlayer(ColorUtils.format(reason));
            } else {
                // Restore state
                target.removePotionEffect(PotionEffectType.BLINDNESS);
                target.resetTitle();
                target.teleport(session.getTargetPreviousLocation());
            }
        }

        if (session.getAdminPlayer() != null) {
            Player admin = Bukkit.getPlayer(session.getAdminPlayer());
            if (admin != null && admin.isOnline()) {
                admin.teleport(session.getAdminPreviousLocation());
                // Optional: restore flight state if we saved it, but for now just leave as is (teleporting usually fixes it if world differs)
            }
        }
    }

    private void buildCage(Location center) {
        // 5x5 cage
        World w = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int y = cy; y <= cy + 4; y++) {
                for (int z = cz - 2; z <= cz + 2; z++) {
                    if (y == cy || y == cy + 4) {
                        w.getBlockAt(x, y, z).setType(Material.BEDROCK);
                    } else if (x == cx - 2 || x == cx + 2 || z == cz - 2 || z == cz + 2) {
                        w.getBlockAt(x, y, z).setType(Material.IRON_BARS);
                    } else {
                        w.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }

    private void startTimerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, PrisonSession> entry : activeSessions.entrySet()) {
                    Player target = Bukkit.getPlayer(entry.getKey());
                    PrisonSession session = entry.getValue();
                    
                    if (target != null && target.isOnline()) {
                        int remaining = session.getRemainingSeconds();
                        if (remaining <= 0) {
                            // Time's up, unprison automatically
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    removeFromServer(target.getUniqueId(), false, "");
                                    target.sendMessage(ColorUtils.format("&aKontrol süreniz bitti, serbestsiniz."));
                                }
                            }.runTask(plugin);
                            continue;
                        }
                        
                        String actionBarMsg = session.getActionbar().replace("%time%", String.valueOf(remaining));
                        target.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtils.format(actionBarMsg)));
                        
                        target.sendTitle(ColorUtils.format(session.getTitle()), ColorUtils.format(session.getSubtitle()), 0, 40, 0);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
