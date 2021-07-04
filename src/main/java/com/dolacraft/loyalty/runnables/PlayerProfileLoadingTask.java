package com.dolacraft.loyalty.runnables;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.datatypes.player.LoyaltyPlayer;
import com.dolacraft.loyalty.datatypes.player.PlayerProfile;
import com.dolacraft.loyalty.util.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerProfileLoadingTask extends BukkitRunnable {
    private static final int MAX_TRIES = 5;
    private final Player player;
    private int attempt = 0;

    public PlayerProfileLoadingTask (Player player) {
        this.player = player;
    }

    public PlayerProfileLoadingTask (Player player, int attempt) {
        this(player);
        this.attempt = attempt;
    }

    @Override
    public void run() {
        // Quit if they logged out
        if (!player.isOnline()) {
            Loyalty.plugin.getLogger().info("Aborting profile loading recovery for " + player.getName() + " - player logged out");
            return;
        }

        // Increment attempt counter and try
        attempt++;

        PlayerProfile profile = Loyalty.getDatabaseManager().loadPlayerProfile(player.getName(), player.getUniqueId(), true);

        if (profile.isLoaded()) {
            new ApplySuccessfulProfile(new LoyaltyPlayer(player, profile)).runTask(Loyalty.plugin);
            return;
        }

        if (attempt >= MAX_TRIES) {
            Loyalty.plugin.getLogger().severe("Giving up on attempting to load the PlayerProfile for " + player.getName());
            Loyalty.plugin.getServer().broadcast(ChatColor.DARK_RED + "Loyalty was unable to load the player data for "
                    + ChatColor.YELLOW + player.getName() + ChatColor.DARK_RED + ". " + ChatColor.LIGHT_PURPLE + "Please inspect your database setup.",
                    Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            player.sendMessage(ChatColor.DARK_RED + "Loyalty cannot load your profile. Please contact server administrators.");
            return;
        }

        new PlayerProfileLoadingTask(player, attempt).runTaskLaterAsynchronously(Loyalty.plugin, 100 * attempt);
    }

    private class ApplySuccessfulProfile extends BukkitRunnable {
        private final LoyaltyPlayer loyaltyPlayer;

        private ApplySuccessfulProfile (LoyaltyPlayer loyaltyPlayer) {
            this.loyaltyPlayer = loyaltyPlayer;
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                Loyalty.plugin.getLogger().info("Aborting profile loading recovery for " + player.getName() + " - player logged out");
                return;
            }

            UserManager.track(loyaltyPlayer);
        }
    }
}
