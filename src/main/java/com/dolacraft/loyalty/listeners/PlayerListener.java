package com.dolacraft.loyalty.listeners;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.datatypes.player.LoyaltyPlayer;
import com.dolacraft.loyalty.managers.PayoutManager;
import com.dolacraft.loyalty.runnables.PlayerProfileLoadingTask;
import com.dolacraft.loyalty.util.Misc;
import com.dolacraft.loyalty.util.UserManager;
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

    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRightClick (PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null) {
            switch (block.getType()) {
                case ACACIA_SIGN:
                case ACACIA_WALL_SIGN:
                case BIRCH_SIGN:
                case BIRCH_WALL_SIGN:
                case CRIMSON_SIGN:
                case CRIMSON_WALL_SIGN:
                case DARK_OAK_SIGN:
                case DARK_OAK_WALL_SIGN:
                case JUNGLE_SIGN:
                case JUNGLE_WALL_SIGN:
                case OAK_SIGN:
                case OAK_WALL_SIGN:
                case SPRUCE_SIGN:
                case SPRUCE_WALL_SIGN:
                case WARPED_SIGN:
                case WARPED_WALL_SIGN:
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Sign sign = (Sign) event.getClickedBlock().getState();

                        if (sign.getLine(0).equalsIgnoreCase("test")) {
                            player.sendMessage("test");
                        }
                    }

                    break;
            }
        }
    }*/
}
