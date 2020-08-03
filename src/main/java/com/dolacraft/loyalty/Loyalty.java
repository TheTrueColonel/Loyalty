package com.dolacraft.loyalty;

import com.dolacraft.loyalty.config.Config;
import com.dolacraft.loyalty.database.DatabaseManager;
import com.dolacraft.loyalty.database.DatabaseManagerFactory;
import com.dolacraft.loyalty.listeners.PlayerListener;
import com.dolacraft.loyalty.runnables.LoyaltyPayoutTask;
import com.dolacraft.loyalty.runnables.SaveTimerTask;
import com.dolacraft.loyalty.util.UserManager;
import com.dolacraft.loyalty.util.commands.CommandRegistrationManager;
import com.google.common.base.Charsets;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;

public final class Loyalty extends JavaPlugin {

    private static DatabaseManager databaseManager;
    private static LoyaltyPayoutTask payoutTask;

    public final static String playerDataKey = "loyalty: Player Date";

    public static FixedMetadataValue metadataValue;
    public static Loyalty plugin;

    public boolean noErrorsInConfigFiles = true;

    @Override
    public void onEnable() {
        try {
            plugin = this;
            metadataValue = new FixedMetadataValue(this, true);

            loadConfigFile();

            if (!noErrorsInConfigFiles) {
                return;
            }

            databaseManager = DatabaseManagerFactory.getDatabaseManager();
            payoutTask = new LoyaltyPayoutTask(this);

            registerEvents();

            scheduleTasks();
            CommandRegistrationManager.registerCommands();
        } catch (Throwable t) {
            getLogger().severe("There was an error while enabling Loyalty!");

            t.printStackTrace();

            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            UserManager.saveAll();
            UserManager.clearAll();
        } catch (NullPointerException ignored) {}

        debug("Cancelling all tasks...");
        getServer().getScheduler().cancelTasks(this);

        databaseManager.onDisable();
    }

    private void loadConfigFile () {
        Config.getInstance();
    }

    private void registerEvents () {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerListener(), this);
    }

    private void scheduleTasks () {
        // Periodic save timer (Saves every 10 minutes by default)
        long saveIntervalTicks = Config.getInstance().getSaveInterval() * 1200;
        new SaveTimerTask().runTaskTimer(this, saveIntervalTicks, saveIntervalTicks);

        // Starts the task to payout LP
        payoutTask.startPayoutTask();
    }

    public void debug (String message) {
        getLogger().info("[Debug] " + message);
    }

    public void error (String message) {
        getLogger().severe("[Error] " + message);
    }

    public InputStreamReader getResourceAsReader (String fileName) {
        InputStream stream = getResource(fileName);
        return stream == null ? null : new InputStreamReader(stream, Charsets.UTF_8);
    }

    public static DatabaseManager getDatabaseManager () {
        return databaseManager;
    }
}
