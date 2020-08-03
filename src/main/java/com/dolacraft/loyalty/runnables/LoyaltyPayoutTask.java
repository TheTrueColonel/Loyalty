package com.dolacraft.loyalty.runnables;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.managers.PayoutManager;
import com.dolacraft.loyalty.util.UserManager;
import org.bukkit.scheduler.BukkitScheduler;

public class LoyaltyPayoutTask {
    private final Loyalty plugin;

    public LoyaltyPayoutTask (Loyalty plugin) {
        this.plugin = plugin;
    }

    public void startPayoutTask () {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        PayoutManager payoutManager = PayoutManager.getInstance();

        scheduler.scheduleSyncRepeatingTask(plugin, () -> payoutManager.getPayoutList().forEach((uuid, time) -> {
            if (time <= System.currentTimeMillis()) {
                UserManager.getPlayer(uuid).getProfile().incrementPoints();

                payoutManager.updatePlayerPayout(uuid);
            }
        }), 0, 20L);
    }
}
