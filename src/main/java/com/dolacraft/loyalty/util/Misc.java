package com.dolacraft.loyalty.util;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.runnables.PlayerProfileLoadingTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public final class Misc {
    public static boolean isNPCEntity (Entity entity) {
        return (entity == null
                || (entity.hasMetadata("NPC") && !(entity instanceof Villager))
                || (entity instanceof NPC && !(entity instanceof Villager))
                || entity.getClass().getName().equalsIgnoreCase("cofh.entity.PlayerFake"));
    }

    public static void profileCleanup (String playerName) {
        Player player = Loyalty.plugin.getServer().getPlayerExact(playerName);

        if (player != null) {
            new PlayerProfileLoadingTask(player).runTaskLaterAsynchronously(Loyalty.plugin, 1);
        }
    }
}
