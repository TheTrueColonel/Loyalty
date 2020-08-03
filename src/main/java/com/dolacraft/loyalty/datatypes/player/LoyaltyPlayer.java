package com.dolacraft.loyalty.datatypes.player;

import com.dolacraft.loyalty.util.UserManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LoyaltyPlayer {
    private Player player;
    private PlayerProfile profile;

    public LoyaltyPlayer (Player player, PlayerProfile profile) {
        UUID uuid = player.getUniqueId();

        this.player = player;
        this.profile = profile;

        if (profile.getUniqueID() == null) {
            profile.setUniqueID(uuid);
        }
    }

    public Player getPlayer () {
        return player;
    }

    public PlayerProfile getProfile () {
        return profile;
    }

    public void logout (boolean syncSave) {
        Player user = getPlayer();

        if (syncSave) {
            getProfile().save();
        } else {
            getProfile().scheduleAsyncSave();
        }

        UserManager.remove(user);
    }
}
