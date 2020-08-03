package com.dolacraft.loyalty.config;

import com.dolacraft.loyalty.Loyalty;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public abstract class ConfigLoader {
    protected static final Loyalty plugin = Loyalty.plugin;

    protected String fileName;
    protected FileConfiguration config;

    private final File configFile;

    public ConfigLoader (String relativePath, String fileName) {
        this.fileName = fileName;
        configFile = new File(plugin.getDataFolder(), relativePath + File.separator + fileName);
        loadFile();
    }

    public ConfigLoader(String fileName) {
        this.fileName = fileName;
        configFile = new File(plugin.getDataFolder(), fileName);
        loadFile();
    }

    protected void loadFile () {
        if (!configFile.exists()) {
            plugin.debug("Creating " + fileName + "...");

            try {
                plugin.saveResource(fileName, false);
            } catch (IllegalArgumentException ex) {
                plugin.saveResource(configFile.getParentFile().getName() + File.separator + fileName, false);
            }
        } else {
            plugin.debug("Loading " + fileName + "...");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    protected boolean validateKeys () {
        return true;
    }

    protected boolean noErrorsInConfig (List<String> issues) {
        for (String issue : issues) {
            plugin.getLogger().warning(issue);
        }

        return issues.isEmpty();
    }

    protected void validate () {
        if (validateKeys()) {
            plugin.debug("No errors found in " + fileName + "!");
        } else {
            plugin.getLogger().warning("Errors were found in " + fileName + "! Loyalty was disabled!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            plugin.noErrorsInConfigFiles = false;
        }
    }
}
