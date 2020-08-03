package com.dolacraft.loyalty.config;

import com.dolacraft.loyalty.database.SQLDatabaseManager;
import com.dolacraft.loyalty.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Config extends AutoUpdateConfigLoader {
    private static Config instance;

    private Config () {
        super("config.yml");
        validate();
    }

    public static Config getInstance () {
        if (instance == null) {
            instance = new Config();
        }

        return instance;
    }

    @Override
    protected boolean validateKeys() {
        List<String> reason = new ArrayList<>();

        /* General Settings */
        if (getSaveInterval() <= 0) {
            reason.add("General.Save_Interval should be greater than 0!");
        }
        if (getPayoutDelay() <= 0) {
            reason.add("General.Payout_Delay should be greater than 0!");
        }

        /* MySQL Settings */
        for (SQLDatabaseManager.PoolIdentifier identifier : SQLDatabaseManager.PoolIdentifier.values()) {
            if (getMySQLMaxConnections(identifier) <= 0) {
                reason.add("MySQL.Database.MaxConnections." + StringUtils.getCapitalized(identifier.toString()) + " should be greater than 0!");
            }
            if (getMySQLMaxPoolSize(identifier) <= 0) {
                reason.add("MySQL.Database.MaxPoolSize." + StringUtils.getCapitalized(identifier.toString()) + " should be greater than 0!");
            }
        }

        return noErrorsInConfig(reason);
    }

    public int getSaveInterval () { return config.getInt("General.Save_Interval", 10); }
    public int getPayoutDelay () { return config.getInt("General.Payout_Delay", 600); }

    public String getMySQLDatabaseName () { return getStringIncludeInts("MySQL.Database.DB_Name"); }
    public String getMySQLUsername () { return getStringIncludeInts("MySQL.Database.Username"); }
    public int getMySQLServerPort () { return config.getInt("MySQL.Server.Port"); }
    public String getMySQLServerName () { return config.getString("MySQL.Server.IP", "localhost"); }
    public String getMySQLServerPassword () { return getStringIncludeInts("MySQL.Database.Password"); }
    public int getMySQLMaxConnections (SQLDatabaseManager.PoolIdentifier identifier) { return config.getInt("MySQL.Database.MaxConnections." + StringUtils.getCapitalized(identifier.toString()), 30); }
    public int getMySQLMaxPoolSize (SQLDatabaseManager.PoolIdentifier identifier) { return config.getInt("MySQL.Database.MaxPoolSize." + StringUtils.getCapitalized(identifier.toString()), 10); }
    public boolean getMySQLSSL () { return config.getBoolean("MySQL.Server.SSL", false); }

    private String getStringIncludeInts (String key) {
        String str = config.getString(key);

        if (str == null) {
            str = String.valueOf(config.getInt(key));
        }

        if (str.equals("0")) {
            str = "No value set for '" + key + "'";
        }

        return str;
    }
}
