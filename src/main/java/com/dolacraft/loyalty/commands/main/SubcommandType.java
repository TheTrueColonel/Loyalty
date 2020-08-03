package com.dolacraft.loyalty.commands.main;

// Enum of all available commands
public enum SubcommandType {
    HELP,
    TOP,
    VERSION,
    NEXT,
    PLAYTIME,
    SET,
    ADD,
    REMOVE;

    public static SubcommandType getSubcommand (String commandName) {
        for (SubcommandType command : values()) {
            if (command.name().equalsIgnoreCase(commandName)) {
                return command;
            }
        }

        if (commandName.equalsIgnoreCase("?")) {
            return HELP;
        } else if (commandName.equalsIgnoreCase("v")) {
            return VERSION;
        }

        return null;
    }
}
