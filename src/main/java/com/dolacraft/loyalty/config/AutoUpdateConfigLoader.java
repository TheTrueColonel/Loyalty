package com.dolacraft.loyalty.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public abstract class AutoUpdateConfigLoader extends ConfigLoader {
    public AutoUpdateConfigLoader (String relativePath, String fileName) {
        super(relativePath, fileName);
    }

    public AutoUpdateConfigLoader (String fileName) {
        super(fileName);
    }

    @Override
    protected void loadFile() {
        super.loadFile();

        FileConfiguration internalConfig = YamlConfiguration.loadConfiguration(plugin.getResourceAsReader(fileName));

        Set<String> configKeys = config.getKeys(true);
        Set<String> internalConfigKeys = internalConfig.getKeys(true);

        boolean needSave = false;

        Set<String> oldKeys = new HashSet<>(configKeys);
        oldKeys.removeAll(internalConfigKeys);

        Set<String> newKeys = new HashSet<>(internalConfigKeys);
        newKeys.removeAll(configKeys);

        if (!newKeys.isEmpty() || !oldKeys.isEmpty()) {
            needSave = true;
        }

        for (String key : oldKeys) {
            plugin.debug("Detected potentially unused key: " + key);
        }

        for (String key : newKeys) {
            plugin.debug("Adding new Key: " + key + " = " + internalConfig.get(key));
            config.set(key, internalConfig.get(key));
        }

        if (needSave) {
            // Get Bukkit's version of an acceptable config with new keys, and no old keys
            String output = config.saveToString();

            // Convert to 4 space indentation
            output = output.replace("  ", "    ");

            // Rip out Bukkit's attempt to save comments at the top of the file
            while (output.replaceAll("[//s]", "").startsWith("#")) {
                output = output.substring(output.indexOf('\n', output.indexOf('#')) + 1);
            }

            try {
                // Read internal
                BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource(fileName)));
                LinkedHashMap<String, String> comments = new LinkedHashMap<>();
                StringBuilder temp = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("#")) {
                        temp.append(line).append("\n");
                    } else if (line.contains(":")) {
                        line = line.substring(0, line.indexOf(":") + 1);

                        if (temp.length() > 0) {
                            if (comments.containsKey(line)) {
                                int index = 0;

                                while (comments.containsKey(line + index)) {
                                    index++;
                                }

                                line = line + index;
                            }

                            comments.put(line, temp.toString());
                            temp = new StringBuilder();
                        }
                    }
                }

                // Dump to the new one
                HashMap<String, Integer> indexed = new HashMap<>();

                for (String key : comments.keySet()) {
                    String actualKey = key.substring(0, key.indexOf(":") + 1);

                    int index = 0;

                    if (indexed.containsKey(actualKey)) {
                        index = indexed.get(actualKey);
                    }

                    boolean isAtTop = !output.contains("\n" + actualKey);

                    index = output.indexOf((isAtTop ? "" : "\n") + actualKey, index);

                    if (index >= 0) {
                        output = output.substring(0, index) + "\n" + comments.get(key) + output.substring(isAtTop ? index : index + 1);
                        indexed.put(actualKey, index + comments.get(key).length() + actualKey.length() + 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Save it
            try {
                String saveName = fileName;

                // At this stage we cannot guarantee that Config has been loaded, so we do that check here
                if (!plugin.getConfig().getBoolean("General.Config_Update_Overwrite", true)) {
                    saveName += ".new";
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(plugin.getDataFolder(), saveName)));

                writer.write(output);
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
