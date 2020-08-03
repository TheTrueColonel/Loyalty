package com.dolacraft.loyalty.util;

import org.bukkit.permissions.Permissible;

public class Permissions {
    public Permissions () {}

    public static boolean userPerms (Permissible permissible) { return permissible.hasPermission("loyalty.user"); }
    public static boolean modPerms (Permissible permissible) { return permissible.hasPermission("loyalty.mod"); }
    public static boolean adminPerms (Permissible permissible) { return permissible.hasPermission("loyalty.admin"); }
}
