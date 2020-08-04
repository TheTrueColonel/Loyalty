package com.dolacraft.loyalty.listeners;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.config.Config;
import com.dolacraft.loyalty.datatypes.player.LoyaltyPlayer;
import com.dolacraft.loyalty.managers.PayoutManager;
import com.dolacraft.loyalty.runnables.PlayerProfileLoadingTask;
import com.dolacraft.loyalty.util.Misc;
import com.dolacraft.loyalty.util.UserManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit (PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!UserManager.hasPlayerDataKey(player)) {
            return;
        }

        LoyaltyPlayer loyaltyPlayer = UserManager.getPlayer(player);
        loyaltyPlayer.logout(false);

        PayoutManager.getInstance().removePlayerPayout(player.getUniqueId());
    }

    /**
     * Monitor PlayerJoinEvents.
     * @param event The event to monitor
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin (PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (Misc.isNPCEntity(player)) {
            return;
        }

        new PlayerProfileLoadingTask(player).runTaskLaterAsynchronously(Loyalty.plugin, 1);

        PayoutManager.getInstance().addPlayerPayout(player.getUniqueId());
    }
}
