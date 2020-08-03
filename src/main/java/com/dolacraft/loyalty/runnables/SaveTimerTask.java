package com.dolacraft.loyalty.runnables;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.datatypes.player.LoyaltyPlayer;
import com.dolacraft.loyalty.util.UserManager;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTimerTask extends BukkitRunnable {

    @Override
    public void run() {
        // All player data will be saved periodically through this
        int count = 1;

        for (LoyaltyPlayer loyaltyPlayer : UserManager.getPlayers()) {
            new PlayerProfileSaveTask(loyaltyPlayer.getProfile()).runTaskLaterAsynchronously(Loyalty.plugin, count);
            count++;
        }
    }
}
