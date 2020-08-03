package com.dolacraft.loyalty.database;

public class DatabaseManagerFactory {
    public static DatabaseManager getDatabaseManager () {
        return new SQLDatabaseManager();
    }
}
