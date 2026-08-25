package com.lypen.prison.listener;

import com.lypen.prison.LypenPrison;
import com.lypen.prison.manager.BanManager;
import com.lypen.prison.manager.PrisonManager;
import com.lypen.prison.model.PrisonSession;
import com.lypen.prison.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerEvents implements Listener {

    private final LypenPrison plugin;

    public PlayerEvents(LypenPrison plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        BanManager bm = plugin.getBanManager();
        if (bm.isBannedByUUID(p.getUniqueId().toString()) || bm.isBannedByIP(p.getAddress().getAddress().getHostAddress())) {
            String msg = plugin.getConfig().getString("quit_ban_message", "&cBanlısınız!");
            p.kickPlayer(ColorUtils.format(msg));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        PrisonManager pm = plugin.getPrisonManager();
        if (pm.isPlayerInPrison(p.getUniqueId())) {
            // Player quit while in prison, ban them
            plugin.getBanManager().banPlayer(p.getUniqueId().toString(), p.getName(), p.getAddress().getAddress().getHostAddress());
            
            // Revert admin
            pm.removeFromServer(p.getUniqueId(), false, ""); 
        }
        
        if (pm.isAdminInPrison(p.getUniqueId())) {
            // Admin quit while in prison, we could release player or do nothing.
            // Requirement says: if admin leaves, admin is teleported back when they join? 
            // Or player is unprisoned.
            // Let's unprison the player just to be safe and restore everything.
            PrisonSession session = pm.getSessionByAdmin(p.getUniqueId());
            if (session != null) {
                pm.removeFromServer(session.getTargetPlayer(), false, "");
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PrisonManager pm = plugin.getPrisonManager();
        
        if (pm.isPlayerInPrison(p.getUniqueId())) {
            PrisonSession session = pm.getSession(p.getUniqueId());
            if (!session.isCanMove()) {
                if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
                    e.setCancelled(true);
                }
            }
        } else if (pm.isAdminInPrison(p.getUniqueId())) {
            // Check bounds (max 15 blocks from center)
            PrisonSession session = pm.getSessionByAdmin(p.getUniqueId());
            Location center = session.getCageCenter();
            Location to = e.getTo();
            if (to.getWorld().equals(center.getWorld())) {
                if (Math.abs(to.getX() - center.getX()) > 15 || 
                    Math.abs(to.getY() - center.getY()) > 15 || 
                    Math.abs(to.getZ() - center.getZ()) > 15) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            PrisonManager pm = plugin.getPrisonManager();
            if (pm.isAdminInPrison(p.getUniqueId())) {
                e.setCancelled(true); // Admin is completely immune in prison
                return;
            }
            if (pm.isPlayerInPrison(p.getUniqueId())) {
                PrisonSession session = pm.getSession(p.getUniqueId());
                if (!session.isCanTakeDamage()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        PrisonManager pm = plugin.getPrisonManager();
        if (pm.isPlayerInPrison(p.getUniqueId())) {
            PrisonSession session = pm.getSession(p.getUniqueId());
            if (!session.isCanChat()) {
                e.setCancelled(true);
                p.sendMessage(ColorUtils.format("&cŞu anda sohbet edemezsiniz!"));
            }
        }
        
        // Disable seeing other players chat? 
        // We can remove jailed players from the recipients of all chat messages.
        e.getRecipients().removeIf(recipient -> pm.isPlayerInPrison(recipient.getUniqueId()) && !pm.getSession(recipient.getUniqueId()).isCanChat());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        PrisonManager pm = plugin.getPrisonManager();
        if (pm.isPlayerInPrison(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(ColorUtils.format("&cKontrol altındayken komut kullanamazsınız!"));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (plugin.getPrisonManager().isPlayerInPrison(e.getPlayer().getUniqueId()) || e.getBlock().getWorld().equals(plugin.getPrisonWorld())) {
            if (!e.getPlayer().hasPermission("lypenprison.admin")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (plugin.getPrisonManager().isPlayerInPrison(e.getPlayer().getUniqueId()) || e.getBlock().getWorld().equals(plugin.getPrisonWorld())) {
            if (!e.getPlayer().hasPermission("lypenprison.admin")) {
                e.setCancelled(true);
            }
        }
    }
}
