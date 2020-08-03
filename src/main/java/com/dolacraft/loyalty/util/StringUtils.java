package com.dolacraft.loyalty.util;

public class StringUtils {
    public static String getCapitalized (String target) {
        return target.substring(0, 1).toUpperCase() + target.substring(1).toLowerCase();
    }
}
