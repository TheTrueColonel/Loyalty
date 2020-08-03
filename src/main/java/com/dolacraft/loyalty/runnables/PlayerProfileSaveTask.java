package com.dolacraft.loyalty.runnables;

import com.dolacraft.loyalty.datatypes.player.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerProfileSaveTask extends BukkitRunnable {
    private final PlayerProfile playerProfile;

    public PlayerProfileSaveTask (PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    @Override
    public void run () {
        playerProfile.save();
    }
}
