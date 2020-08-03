package com.dolacraft.loyalty.database;

import com.dolacraft.loyalty.datatypes.player.PlayerProfile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DatabaseManager {
    /**
     * Remove a user from the database.
     *
     * @param playerName The name of the user to remove
     * @return true if the user was successfully removed, false otherwise
     */
    boolean removeUser(String playerName);

    /**
     * Save a user to the database.
     *
     * @param profile The profile of the player to save
     * @return true if successful, false on failure
     */
    boolean saveUser(PlayerProfile profile);

    /**
     * Add a new user to the database.
     *
     * @param playerName The name of the player to be added to the database
     * @param uuid The uuid of the player to be added to the database
     */
    void newUser(String playerName, UUID uuid);

    /**
     * Load a player from the database.
     *
     * @param uuid The uuid of the player to load from the database
     * @return The player's data, or an unloaded PlayerProfile if not found
     */
    PlayerProfile loadPlayerProfile(UUID uuid);

    /**
     * Load a player from the database. Attempt to use uuid, fall back on playername
     *
     * @param playerName The name of the player to load from the database
     * @param uuid The uuid of the player to load from the database
     * @param createNew Whether to create a new record if the player is not
     *          found
     * @return The player's data, or an unloaded PlayerProfile if not found
     *          and createNew is false
     */
    PlayerProfile loadPlayerProfile(String playerName, UUID uuid, boolean createNew);

    /**
     * Get all users currently stored in the database.
     *
     * @return list of playernames
     */
    List<String> getStoredUsers();

    boolean saveUserUUID(String userName, UUID uuid);

    boolean saveUserUUIDs(Map<String, UUID> fetchedUUIDs);

    Map<String, Integer> getTopUsers();

    /**
     * Called when the plugin disables
     */
    void onDisable();
}
