package com.dolacraft.loyalty.util;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.datatypes.player.LoyaltyPlayer;
import com.google.common.collect.ImmutableList;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class UserManager {
    private UserManager () {}

    /**
     * Track a new user.
     *
     * @param loyaltyPlayer The player profile to start tracking
     */
    public static void track (LoyaltyPlayer loyaltyPlayer) {
        loyaltyPlayer.getPlayer().setMetadata(Loyalty.playerDataKey, new FixedMetadataValue(Loyalty.plugin, loyaltyPlayer));
    }

    /**
     * Remove a player.
     *
     * @param player The Player object.
     */
    public static void remove (Player player) {
        player.removeMetadata(Loyalty.playerDataKey, Loyalty.plugin);
    }

    /**
     * Clear all users.
     */
    public static void clearAll () {
        for (Player player : Loyalty.plugin.getServer().getOnlinePlayers()) {
            remove(player);
        }
    }

    public static void saveAll () {
        ImmutableList<Player> onlinePlayers = ImmutableList.copyOf(Loyalty.plugin.getServer().getOnlinePlayers());
        Loyalty.plugin.debug("Save LoyaltyPlayers... (" + onlinePlayers.size() + ")");

        for (Player player: onlinePlayers) {
            try {
                getPlayer(player).getProfile().save();
            } catch (Exception e) {
                Loyalty.plugin.getLogger().warning("Could not save Loyalty player data for player: " + player.getName());
            }
        }
    }

    public static Collection<LoyaltyPlayer> getPlayers () {
        Collection<LoyaltyPlayer> playerCollection = new ArrayList<>();

        for (Player player : Loyalty.plugin.getServer().getOnlinePlayers()) {
            if (hasPlayerDataKey(player)) {
                playerCollection.add(getPlayer(player));
            }
        }

        return playerCollection;
    }

    public static LoyaltyPlayer getPlayer (String playerName) {
        return retrieveLoyaltyPlayer(playerName, false);
    }

    public static LoyaltyPlayer getPlayer (Player player) {
        return (LoyaltyPlayer) player.getMetadata(Loyalty.playerDataKey).get(0).value();
    }

    public static LoyaltyPlayer getPlayer (UUID player) {
        return retrieveLoyaltyPlayer(player, false);
    }

    public static LoyaltyPlayer getOfflinePlayer (OfflinePlayer player) {
        if (player instanceof Player) {
            return getPlayer((Player) player);
        }

        return retrieveLoyaltyPlayer(player.getName(), true);
    }

    public static LoyaltyPlayer getOfflinePlayer (String playerName) {
        return  retrieveLoyaltyPlayer(playerName, true);
    }

    public static LoyaltyPlayer getOfflinePlayer (UUID player) {
        return retrieveLoyaltyPlayer(player, true);
    }

    public static LoyaltyPlayer retrieveLoyaltyPlayer (String playerName, boolean offlineValid) {
        Player player = Loyalty.plugin.getServer().getPlayerExact(playerName);

        if (player == null) {
            if (!offlineValid) {
                Loyalty.plugin.getLogger().warning("A valid LoyaltyPlayer object could not be found for " + playerName + ".");
            }

            return null;
        }

        return getPlayer(player);
    }

    public static LoyaltyPlayer retrieveLoyaltyPlayer (UUID playerUuid, boolean offlineValid) {
        Player player = Loyalty.plugin.getServer().getPlayer(playerUuid);

        if (player == null) {
            if (!offlineValid) {
                Loyalty.plugin.getLogger().warning("A valid LoyaltyPlayer object could not be found for {" + playerUuid + "}.");
            }

            return null;
        }

        return getPlayer(player);
    }

    public static boolean hasPlayerDataKey (Entity entity) {
        return entity != null && entity.hasMetadata(Loyalty.playerDataKey);
    }

    public static Map<String, Integer> getTopPlayers () {
        return Loyalty.getDatabaseManager().getTopUsers();
    }
}
