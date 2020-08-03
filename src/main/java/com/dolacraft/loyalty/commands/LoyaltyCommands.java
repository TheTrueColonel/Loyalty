package com.dolacraft.loyalty.commands;

import com.dolacraft.loyalty.commands.main.SubcommandType;
import com.dolacraft.loyalty.datatypes.player.LoyaltyPlayer;
import com.dolacraft.loyalty.managers.PayoutManager;
import com.dolacraft.loyalty.util.Permissions;
import com.dolacraft.loyalty.util.UserManager;
import com.dolacraft.loyalty.util.commands.CommandRegistrationManager;
import com.dolacraft.loyalty.util.commands.CommandUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LoyaltyCommands implements TabExecutor {
    private static final List<String> LOYALTY_SUBCOMMANDS;

    private final String commandPrefix = ChatColor.GOLD + "[Loyalty] " + ChatColor.DARK_AQUA;

    static {
        ArrayList<String> subcommands = new ArrayList<>();

        for (SubcommandType type : SubcommandType.values()) {
            subcommands.add(type.toString().toLowerCase());
        }

        subcommands.add("v");
        subcommands.add("?");

        Collections.sort(subcommands);
        LOYALTY_SUBCOMMANDS = ImmutableList.copyOf(subcommands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (CommandUtils.noConsoleUsage(sender)) {
            return true;
        }

        Player player = (Player) sender;

        if (!UserManager.hasPlayerDataKey(player)) {
            return true;
        }

        LoyaltyPlayer loyaltyPlayer = UserManager.getPlayer(player);

        if (args.length < 1) {
            int points = loyaltyPlayer.getProfile().getPoints();

            player.sendMessage(commandPrefix + "You have " + ChatColor.AQUA + points + ChatColor.DARK_AQUA + " loyalty points");

            return true;
        }

        SubcommandType subcommand = SubcommandType.getSubcommand(args[0]);

        if (subcommand == null) {
            LoyaltyPlayer checkedPlayer = UserManager.getPlayer(args[0]);

            if (checkedPlayer == null) {
                player.sendMessage(commandPrefix + ChatColor.RED + "Player not found");

                return true;
            }
            int points = checkedPlayer.getProfile().getPoints();

            player.sendMessage(commandPrefix + args[0] + " has " + ChatColor.AQUA + points + ChatColor.DARK_AQUA + " loyalty points");

            return true;
        }

        switch (subcommand) {
            case HELP:
                if (!Permissions.userPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                player.sendMessage(ChatColor.AQUA + "---------" + ChatColor.GOLD + " Loyalty Help " + ChatColor.AQUA + "---------\n" +
                        ChatColor.GOLD + "/loyalty [/lt]" + ChatColor.AQUA + " - checks your loyalty points\n" +
                        ChatColor.GOLD + "/lt <username>" + ChatColor.AQUA + " - checks another player's loyalty points\n" +
                        ChatColor.GOLD + "/lt help [/lt ?]" + ChatColor.AQUA + " - shows this help menu\n" +
                        ChatColor.GOLD + "/lt top" + ChatColor.AQUA + " - shows the top 10 players\n" +
                        ChatColor.GOLD + "/lt version [/lt v] " + ChatColor.AQUA + " - shows current plugin version\n" +
                        ChatColor.GOLD + "/lt next" + ChatColor.AQUA + " - shows your next payout\n" +
                        ChatColor.GOLD + "/lt playtime" + ChatColor.AQUA + " - shows current playtime\n" +
                        ChatColor.GOLD + "/lt set <username> <amount>" + ChatColor.AQUA + " - sets player's points\n" +
                        ChatColor.GOLD + "/lt add <username> <amount>" + ChatColor.AQUA + " - adds amount to player's points\n" +
                        ChatColor.GOLD + "/lt remove <username> <amount>" + ChatColor.AQUA + " - removes amount from player's points\n" +
                        ChatColor.AQUA + "---------" + ChatColor.GOLD + " Loyalty Help " + ChatColor.AQUA + "---------");

                return true;
            case TOP:
                if (!Permissions.userPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                Map<String, Integer> topPlayers = UserManager.getTopPlayers();
                Map<String, Integer> topPlayersSorted =
                        topPlayers.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(10)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

                StringBuilder builder = new StringBuilder();

                AtomicInteger i = new AtomicInteger(1);

                builder.append(commandPrefix).append("Top 10 players:\n");

                topPlayersSorted.forEach((user, points) -> {
                    builder.append(ChatColor.GOLD)
                            .append(i.get())
                            .append(". ")
                            .append(ChatColor.AQUA)
                            .append(user)
                            .append(ChatColor.GOLD)
                            .append(" | ")
                            .append(ChatColor.AQUA)
                            .append("Points: ")
                            .append(ChatColor.GOLD)
                            .append(points)
                            .append("\n");

                    i.getAndIncrement();
                });

                player.sendMessage(builder.toString());

                return true;
            case VERSION:
                if (!Permissions.userPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                player.sendMessage(commandPrefix + "v1.0.1");

                return true;
            case NEXT:
                if (!Permissions.userPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                long payoutTime = PayoutManager.getInstance().getPayoutTime(player.getUniqueId()) - System.currentTimeMillis();
                long payoutMinutes = TimeUnit.MILLISECONDS.toMinutes(payoutTime) % 60;
                long payoutSeconds = TimeUnit.MILLISECONDS.toSeconds(payoutTime) % 60;

                if (payoutTime > 0) {
                    player.sendMessage(commandPrefix + "Payment time: " + payoutMinutes + "m " + payoutSeconds + "s");
                } else {
                    player.sendMessage(commandPrefix + "Payment time: Now.");
                }

                return true;
            case PLAYTIME:
                if (!Permissions.userPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                int playtimeInSec = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
                long days = TimeUnit.SECONDS.toDays(playtimeInSec);
                long hours = TimeUnit.SECONDS.toHours(playtimeInSec) % 24;
                long minutes = TimeUnit.SECONDS.toMinutes(playtimeInSec) % 60;
                int seconds = playtimeInSec % 60;

                if (days > 0) {
                    player.sendMessage(commandPrefix + "You have been online for: " + days + "d " + hours + "h " + minutes + "m " + seconds + "s");
                    return true;
                }

                player.sendMessage(commandPrefix + "You have been online for: " + hours + "h " + minutes + "m " + seconds + "s");
                return true;
            case SET:
                if (!Permissions.adminPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                if (args.length == 3) {
                    LoyaltyPlayer selectedPlayer = UserManager.getPlayer(args[1]);

                    if (selectedPlayer != null) {
                        if (Integer.parseInt(args[2]) <= 0) {
                            player.sendMessage(commandPrefix + ChatColor.RED + "Amount must be greater than 0");
                            return true;
                        }

                        selectedPlayer.getProfile().setPoints(Integer.parseInt(args[2]));

                        player.sendMessage(commandPrefix + "Set " + args[1] + "'s points to " + args[2]);

                        selectedPlayer.getProfile().save();
                    } else {
                        player.sendMessage(commandPrefix + ChatColor.RED + "Player not found");
                    }
                } else {
                    player.sendMessage(commandPrefix + ChatColor.RED + "Command usage: /lt set <username> <amount>");
                }
                return true;
            case ADD:
                if (!Permissions.adminPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                if (args.length == 3) {
                    LoyaltyPlayer selectedPlayer = UserManager.getPlayer(args[1]);

                    if (selectedPlayer != null) {
                        if (Integer.parseInt(args[2]) <= 0) {
                            player.sendMessage(commandPrefix + ChatColor.RED + "Amount must be greater than 0");
                            return true;
                        }

                        selectedPlayer.getProfile().incrementPoints(Integer.parseInt(args[2]));

                        builder = new StringBuilder();
                        builder.append(commandPrefix)
                                .append("Added ")
                                .append(args[2]);

                        if (Integer.parseInt(args[2]) > 1) {
                            builder.append(" points to ");
                        } else {
                            builder.append(" point to ");
                        }

                        builder.append(args[1]);

                        player.sendMessage(builder.toString());

                        selectedPlayer.getProfile().save();
                    } else {
                        player.sendMessage(commandPrefix + ChatColor.RED + "Player not found");
                    }
                } else {
                    player.sendMessage(commandPrefix + ChatColor.RED + "Command usage: /lt add <username> <amount>");
                }
                return true;
            case REMOVE:
                if (!Permissions.modPerms(sender)) {
                    player.sendMessage(CommandRegistrationManager.permissionsMessage);
                    return true;
                }

                if (args.length == 3) {
                    LoyaltyPlayer selectedPlayer = UserManager.getPlayer(args[1]);

                    if (selectedPlayer != null) {
                        if (Integer.parseInt(args[2]) <= 0) {
                            player.sendMessage(commandPrefix + ChatColor.RED + "Amount must be greater than 0");
                            return true;
                        }

                        selectedPlayer.getProfile().removePoints(Integer.parseInt(args[2]));

                        builder = new StringBuilder();
                        builder.append(commandPrefix)
                                .append("Removed ")
                                .append(args[2]);

                        if (Integer.parseInt(args[2]) > 1) {
                            builder.append(" points from ");
                        } else {
                            builder.append(" point from ");
                        }

                        builder.append(args[1]);

                        player.sendMessage(builder.toString());

                        selectedPlayer.getProfile().save();
                    } else {
                        player.sendMessage(commandPrefix + ChatColor.RED + "Player not found");
                    }
                } else {
                    player.sendMessage(commandPrefix + ChatColor.RED + "Command usage: /lt remove <username> <amount>");
                }
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return StringUtil.copyPartialMatches(args[0], LOYALTY_SUBCOMMANDS, new ArrayList<>(LOYALTY_SUBCOMMANDS.size()));
            case 2:
                SubcommandType subcommand = SubcommandType.getSubcommand(args[0]);

                if (subcommand == null) {
                    return ImmutableList.of();
                }

                switch (subcommand) {
                    case ADD:
                    case SET:
                    case REMOVE:

                        List<String> playerNames = CommandUtils.getOnlinePlayerNames(sender);
                        return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>(playerNames.size()));
                    default:
                        return ImmutableList.of();
                }
            default:
                return ImmutableList.of();
        }
    }
}
