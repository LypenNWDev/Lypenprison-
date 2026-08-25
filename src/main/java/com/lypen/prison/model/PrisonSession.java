package com.lypen.prison.model;

import org.bukkit.Location;
import java.util.UUID;

public class PrisonSession {
    private final UUID targetPlayer;
    private final UUID adminPlayer; // Can be null if console executed? Let's assume always a player or nullable.
    private final String prisonType;
    private final int durationSeconds;
    private final long startTime;
    private final Location targetPreviousLocation;
    private final Location adminPreviousLocation;
    private final Location cageCenter;
    
    // config settings for the type
    private final boolean applyBlindness;
    private final boolean canTakeDamage;
    private final boolean canMove;
    private final boolean canChat;
    
    private final String title;
    private final String subtitle;
    private final String actionbar;

    public PrisonSession(UUID targetPlayer, UUID adminPlayer, String prisonType, int durationSeconds, 
                         Location targetPreviousLocation, Location adminPreviousLocation, Location cageCenter,
                         boolean applyBlindness, boolean canTakeDamage, boolean canMove, boolean canChat,
                         String title, String subtitle, String actionbar) {
        this.targetPlayer = targetPlayer;
        this.adminPlayer = adminPlayer;
        this.prisonType = prisonType;
        this.durationSeconds = durationSeconds;
        this.startTime = System.currentTimeMillis();
        this.targetPreviousLocation = targetPreviousLocation;
        this.adminPreviousLocation = adminPreviousLocation;
        this.cageCenter = cageCenter;
        
        this.applyBlindness = applyBlindness;
        this.canTakeDamage = canTakeDamage;
        this.canMove = canMove;
        this.canChat = canChat;
        this.title = title;
        this.subtitle = subtitle;
        this.actionbar = actionbar;
    }

    public UUID getTargetPlayer() { return targetPlayer; }
    public UUID getAdminPlayer() { return adminPlayer; }
    public String getPrisonType() { return prisonType; }
    public int getDurationSeconds() { return durationSeconds; }
    
    public int getRemainingSeconds() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        return (int) Math.max(0, durationSeconds - elapsed);
    }

    public Location getTargetPreviousLocation() { return targetPreviousLocation; }
    public Location getAdminPreviousLocation() { return adminPreviousLocation; }
    public Location getCageCenter() { return cageCenter; }

    public boolean isApplyBlindness() { return applyBlindness; }
    public boolean isCanTakeDamage() { return canTakeDamage; }
    public boolean isCanMove() { return canMove; }
    public boolean isCanChat() { return canChat; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getActionbar() { return actionbar; }
}
