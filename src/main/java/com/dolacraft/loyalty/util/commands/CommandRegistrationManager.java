package com.dolacraft.loyalty.util.commands;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.commands.LoyaltyCommands;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistrationManager {
    private CommandRegistrationManager() {}

    public static final String permissionsMessage = ChatColor.GOLD + "[Loyalty]" + ChatColor.DARK_AQUA + " Insufficient permissions";

    private static void registerLoyaltyCommand() {
        List<String> aliases = new ArrayList<>();
        aliases.add("lt");

        PluginCommand command = Loyalty.plugin.getCommand("loyalty");
        assert command != null;
        command.setAliases(aliases);
        command.setDescription("Basic Loyalty command.");
        command.setPermission("loyalty.user;loyalty.mod;loyalty.admin");
        command.setPermissionMessage(permissionsMessage);
        command.setExecutor(new LoyaltyCommands());
    }

    public static void registerCommands () {
        registerLoyaltyCommand();
    }
}
