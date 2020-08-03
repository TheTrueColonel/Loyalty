package com.dolacraft.loyalty.datatypes.player;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.runnables.PlayerProfileSaveTask;

import java.util.UUID;

public class PlayerProfile {
    private final String playerName;
    private UUID uuid;
    private int points;
    private boolean loaded;
    private volatile boolean changed;

    @Deprecated
    public PlayerProfile(String playerName) {
        this(playerName, null);
    }

    public PlayerProfile (String playerName, UUID uuid) {
        this.playerName = playerName;
        this.uuid = uuid;
    }

    @Deprecated
    public PlayerProfile (String playerName, boolean isLoaded) {
        this(playerName);
        this.loaded = isLoaded;
    }

    public PlayerProfile (String playerName, UUID uuid, boolean isLoaded) {
        this(playerName, uuid);
        this.loaded = isLoaded;
    }

    public PlayerProfile (String playerName, UUID uuid, int points, boolean isLoaded) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.points = points;

        this.loaded = isLoaded;
    }

    public void scheduleAsyncSave () {
        new PlayerProfileSaveTask(this).runTaskAsynchronously(Loyalty.plugin);
    }

    public void save () {
        if (!changed || !loaded) {
            return;
        }

        PlayerProfile profileCopy = new PlayerProfile(playerName, uuid, points, true);
        changed = !Loyalty.getDatabaseManager().saveUser(profileCopy);

        if (changed) {
            Loyalty.plugin.getLogger().warning("PlayerProfile saving failed for player: " + playerName + " " + uuid);
        }
    }

    public String getPlayerName () {
        return playerName;
    }

    public UUID getUniqueID () {
        return uuid;
    }

    public void setUniqueID (UUID uuid) {
        changed = true;

        this.uuid = uuid;
    }

    public int getPoints () {
        return points;
    }

    public void setPoints (int value) {
        changed = true;

        points = value;
    }

    public void incrementPoints () {
        changed = true;

        points++;
    }

    public void incrementPoints (int amount) {
        changed = true;

        points += amount;
    }

    public void removePoints (int amount) {
        changed = true;

        points -= amount;
    }

    // TODO: Usage for later, maybe
    public Integer purchaseWithPoints (int amount) {
        if (points < amount) {
            return null;
        }

        changed = true;

        points -= amount;
        return points;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
