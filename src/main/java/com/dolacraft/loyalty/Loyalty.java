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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Loyalty extends JavaPlugin {
    private static DatabaseManager databaseManager;
    private static LoyaltyPayoutTask payoutTask;

    public final static String playerDataKey = "loyalty: Player Date";

    public static final Properties properties = new Properties();

    public static FixedMetadataValue metadataValue;
    public static Loyalty plugin;

    public Inventory storeInventory;
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

            properties.load(this.getClassLoader().getResourceAsStream(".properties"));

            createStoreInventory();

            registerEvents();
            scheduleTasks();

            CommandRegistrationManager.registerCommands(storeInventory);
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

        pluginManager.registerEvents(new PlayerListener(storeInventory), this);
    }

    private void scheduleTasks () {
        // Periodic save timer (Saves every 10 minutes by default)
        long saveIntervalTicks = Config.getInstance().getSaveInterval() * 1200;
        new SaveTimerTask().runTaskTimer(this, saveIntervalTicks, saveIntervalTicks);

        // Starts the task to payout LP
        payoutTask.startPayoutTask();
    }

    private void createStoreInventory () {
        storeInventory = Bukkit.createInventory(null, 27, "Loyalty Store");

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        List<String> lore = new ArrayList<>();

        meta.setDisplayName(ChatColor.AQUA + "Store test: Buy");
        lore.add(ChatColor.GRAY + "Click to test the buy function!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        storeInventory.setItem(13, item);
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
