package com.dolacraft.loyalty.util.commands;

import com.dolacraft.loyalty.Loyalty;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CommandUtils {

    public static boolean noConsoleUsage (CommandSender sender) {
        if (sender instanceof Player) {
            return false;
        }

        sender.sendMessage("This command does not support console usage.");
        return true;
    }

    public static List<String> getOnlinePlayerNames (CommandSender sender) {
        Player player = sender instanceof Player ? (Player) sender : null;
        List<String> onlinePlayerNames = new ArrayList<>();

        for (Player onlinePlayer : Loyalty.plugin.getServer().getOnlinePlayers()) {
            if (player != null && player.canSee(onlinePlayer)) {
                onlinePlayerNames.add(onlinePlayer.getName());
            }
        }

        return onlinePlayerNames;
    }
}
