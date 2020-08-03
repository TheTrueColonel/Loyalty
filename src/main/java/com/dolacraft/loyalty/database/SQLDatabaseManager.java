package com.dolacraft.loyalty.database;

import com.dolacraft.loyalty.Loyalty;
import com.dolacraft.loyalty.config.Config;
import com.dolacraft.loyalty.datatypes.player.PlayerProfile;
import com.dolacraft.loyalty.util.Misc;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.*;
import java.util.*;

public final class SQLDatabaseManager implements DatabaseManager {

    private final Map<UUID, Integer> cachedUserIDs = new HashMap<>();

    private DataSource miscPool;
    private DataSource loadPool;
    private DataSource savePool;

    protected SQLDatabaseManager () {
        String connectionString = "jdbc:mysql://" + Config.getInstance().getMySQLServerName() + ":" + Config.getInstance().getMySQLServerPort() + "/" + Config.getInstance().getMySQLDatabaseName();

        if (Config.getInstance().getMySQLSSL()) {
            connectionString +=
                    "?verifyServerCertificate=false"+
                            "&useSSL=true"+
                            "&requireSSL=true";
        } else {
            connectionString +=
                    "?useSSL=false";
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDriverClassName("com.mysql.jdbc.Driver");
        poolProperties.setUrl(connectionString);
        poolProperties.setUsername(Config.getInstance().getMySQLUsername());
        poolProperties.setPassword(Config.getInstance().getMySQLServerPassword());
        poolProperties.setMaxIdle(Config.getInstance().getMySQLMaxPoolSize(PoolIdentifier.MISC));
        poolProperties.setMaxActive(Config.getInstance().getMySQLMaxConnections(PoolIdentifier.MISC));
        poolProperties.setInitialSize(0);
        poolProperties.setMaxWait(-1);
        poolProperties.setRemoveAbandoned(true);
        poolProperties.setRemoveAbandonedTimeout(60);
        poolProperties.setTestOnBorrow(true);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setValidationInterval(30000);
        miscPool = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
        poolProperties = new PoolProperties();
        poolProperties.setDriverClassName("com.mysql.jdbc.Driver");
        poolProperties.setUrl(connectionString);
        poolProperties.setUsername(Config.getInstance().getMySQLUsername());
        poolProperties.setPassword(Config.getInstance().getMySQLServerPassword());
        poolProperties.setInitialSize(0);
        poolProperties.setMaxIdle(Config.getInstance().getMySQLMaxPoolSize(PoolIdentifier.SAVE));
        poolProperties.setMaxActive(Config.getInstance().getMySQLMaxConnections(PoolIdentifier.SAVE));
        poolProperties.setMaxWait(-1);
        poolProperties.setRemoveAbandoned(true);
        poolProperties.setRemoveAbandonedTimeout(60);
        poolProperties.setTestOnBorrow(true);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setValidationInterval(30000);
        savePool = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
        poolProperties = new PoolProperties();
        poolProperties.setDriverClassName("com.mysql.jdbc.Driver");
        poolProperties.setUrl(connectionString);
        poolProperties.setUsername(Config.getInstance().getMySQLUsername());
        poolProperties.setPassword(Config.getInstance().getMySQLServerPassword());
        poolProperties.setInitialSize(0);
        poolProperties.setMaxIdle(Config.getInstance().getMySQLMaxPoolSize(PoolIdentifier.LOAD));
        poolProperties.setMaxActive(Config.getInstance().getMySQLMaxConnections(PoolIdentifier.LOAD));
        poolProperties.setMaxWait(-1);
        poolProperties.setRemoveAbandoned(true);
        poolProperties.setRemoveAbandonedTimeout(60);
        poolProperties.setTestOnBorrow(true);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setValidationInterval(30000);
        loadPool = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);

        checkStructure();
    }

    public boolean removeUser(String playerName) {
        boolean success = false;

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement("DELETE FROM u " +
                    "USING loyalty_users u " +
                    "WHERE u.user = ?");

            statement.setString(1, playerName);

            success = statement.executeUpdate() != 0;
        } catch (SQLException e) {
            printErrors(e);
        } finally {
            tryClose(statement);
            tryClose(connection);
        }

        if (success) {
            Misc.profileCleanup(playerName);
        }

        return success;
    }

    public boolean saveUser(PlayerProfile profile) {
        boolean success = true;
        PreparedStatement statement = null;
        Connection connection = null;

        try {
            connection = getConnection(PoolIdentifier.SAVE);

            int id = getUserID(connection, profile.getPlayerName(), profile.getUniqueID());

            if (id == -1) {
                id = newUser(connection, profile.getPlayerName(), profile.getUniqueID());

                if (id == -1) {
                    Loyalty.plugin.getLogger().severe("Failed to create new account for " + profile.getPlayerName());
                    return false;
                }
            }

            statement = connection.prepareStatement("UPDATE loyalty_users SET points = ? WHERE id = ?");
            statement.setInt(1, profile.getPoints());
            statement.setInt(2, id);
            success = (statement.executeUpdate() != 0);
            statement.close();

            if (!success) {
                Loyalty.plugin.getLogger().severe("Failed to update points for " + profile.getPlayerName());
                return false;
            }
        } catch (SQLException e) {
            printErrors(e);
        } finally {
            tryClose(statement);
            tryClose(connection);
        }

        return success;
    }

    public void newUser(String playerName, UUID uuid) {
        Connection connection = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            newUser(connection, playerName, uuid);
        } catch (SQLException ex) {
            printErrors(ex);
        } finally {
            tryClose(connection);
        }
    }

    private int newUser(Connection connection, String playerName, UUID uuid) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(
                    "UPDATE `loyalty_users` "
                            + "SET user = ? "
                            + "WHERE user = ?");
            statement.setString(1, "_INVALID_OLD_USERNAME_");
            statement.setString(2, playerName);
            statement.executeUpdate();
            statement.close();
            statement = connection.prepareStatement("INSERT INTO loyalty_users (user, uuid, points) VALUES (?, ?, 0)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, playerName);
            statement.setString(2, uuid != null ? uuid.toString() : null);
            statement.executeUpdate();

            resultSet = statement.getGeneratedKeys();

            if (!resultSet.next()) {
                Loyalty.plugin.getLogger().severe("Unable to create new user account in DB");
                return -1;
            }

            return resultSet.getInt(1);
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            tryClose(resultSet);
            tryClose(statement);
        }
        return -1;
    }

    public PlayerProfile loadPlayerProfile(UUID uuid) {
        return loadPlayerProfile("", uuid, false, true);
    }

    public PlayerProfile loadPlayerProfile (String playerName, UUID uuid, boolean create) {
        return loadPlayerProfile(playerName, uuid, create, true);
    }

    private PlayerProfile loadPlayerProfile (String playerName, UUID uuid, boolean create, boolean retry) {
        PreparedStatement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(PoolIdentifier.LOAD);
            int id = getUserID(connection, playerName, uuid);

            if (id == -1) {
                // There is no such user
                if (create) {
                    id = newUser(connection, playerName, uuid);
                    create = false;
                    if (id == -1) {
                        return new PlayerProfile(playerName, false);
                    }
                } else {
                    return new PlayerProfile(playerName, false);
                }
            }
            // There is such a user
            statement = connection.prepareStatement("SELECT " +
                    "u.uuid, u.points, u.user " +
                    "FROM loyalty_users u " +
                    "WHERE u.id = ?");

            statement.setInt(1, id);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                try {
                    PlayerProfile profile = loadFromResult(playerName, resultSet);
                    String name = resultSet.getString(3);

                    resultSet.close();
                    statement.close();

                    if (uuid != null && !playerName.isEmpty() && !playerName.equalsIgnoreCase(name)) {
                        statement = connection.prepareStatement("UPDATE `loyalty_users` " +
                                "SET user = ?" +
                                "WHERE user = ?");
                        statement.setString(1, "_INVALID_OLD_USERNAME_");
                        statement.setString(2, name);
                        statement.executeUpdate();
                        statement.close();
                        statement = connection.prepareStatement(
                                "UPDATE `loyalty_users` "
                                        + "SET user = ?, uuid = ? "
                                        + "WHERE id = ?");
                        statement.setString(1, playerName);
                        statement.setString(2, uuid.toString());
                        statement.setInt(3, id);
                        statement.executeUpdate();
                        statement.close();
                    }

                    return profile;
                } catch (SQLException e) {
                    printErrors(e);
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            printErrors(e);
        } finally {
            tryClose(resultSet);
            tryClose(statement);
            tryClose(connection);
        }

        // Problem, nothing was returned

        // Return unloaded profile
        if (!retry) {
            return  new PlayerProfile(playerName, false);
        }

        // Retry and abort on re-failure
        return loadPlayerProfile(playerName, uuid, create, false);
    }

    public boolean saveUserUUID(String userName, UUID uuid) {
        PreparedStatement statement = null;
        Connection connection = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement("UPDATE `loyalty_users` SET " +
                    "uuid = ? WHERE user = ?");
            statement.setString(1, uuid.toString());
            statement.setString(2, userName);
            statement.execute();
            return true;
        } catch (SQLException e) {
            printErrors(e);
            return false;
        } finally {
            tryClose(statement);
            tryClose(connection);
        }
    }

    public boolean saveUserUUIDs(Map<String, UUID> fetchedUUIDs) {
        PreparedStatement statement = null;
        Connection connection = null;

        int count = 0;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement("UPDATE loyalty_users SET uuid = ? WHERE user = ?");

            for (Map.Entry<String, UUID> entry : fetchedUUIDs.entrySet()) {
                statement.setString(1, entry.getValue().toString());
                statement.setString(2, entry.getKey());

                statement.addBatch();

                count++;

                if ((count % 500) == 0) {
                    statement.executeBatch();
                    count = 0;
                }
            }

            return true;
        } catch (SQLException e) {
            printErrors(e);
            return false;
        } finally {
            tryClose(statement);
            tryClose(connection);
        }
    }

    public Map<String, Integer> getTopUsers () {
        PreparedStatement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;

        Map<String, Integer> results = new HashMap<>();

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement("SELECT user, points FROM loyalty_users " +
                    "WHERE points > 0 ORDER BY points LIMIT 10");

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                results.put(resultSet.getString(1),
                        Integer.parseInt(resultSet.getString(2)));
            }

            resultSet.close();
        } catch (SQLException e) {
            printErrors(e);
            return null;
        } finally {
            tryClose(resultSet);
            tryClose(statement);
            tryClose(connection);
        }

        return results;
    }

    public List<String> getStoredUsers() {
        ArrayList<String> users = new ArrayList<>();

        Statement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT user FROM loyalty_users");
            while (resultSet.next()) {
                users.add(resultSet.getString("user"));
            }
        } catch (SQLException e) {
            printErrors(e);
        } finally {
            tryClose(resultSet);
            tryClose(statement);
            tryClose(connection);
        }

        return users;
    }

    private void checkStructure () {
        PreparedStatement statement = null;
        Statement createStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        try {
            connection = getConnection(PoolIdentifier.MISC);
            statement = connection.prepareStatement("SELECT table_name FROM INFORMATION_SCHEMA.TABLES"
                    + " WHERE table_schema = ?"
                    + " AND table_name = ?");
            statement.setString(1, Config.getInstance().getMySQLDatabaseName());
            statement.setString(2, "loyalty_users");
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                createStatement = connection.createStatement();
                createStatement.executeUpdate("CREATE TABLE IF NOT EXISTS `loyalty_users` ("
                    + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                    + "`user` varchar(40) NOT NULL,"
                    + "`uuid` varchar(36) NULL DEFAULT NULL,"
                    + "`points` varchar(4) NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "INDEX(`user`(20) ASC),"
                    + "UNIQUE KEY `uuid` (`uuid`)) DEFAULT CHARSET=utf8 AUTO_INCREMENT=1");
                tryClose(createStatement);
            }
            tryClose(resultSet);
            tryClose(statement);
        } catch (SQLException e) {
            printErrors(e);

            Loyalty.plugin.error("There was a problem connecting to the database! Please check the database settings in config.yml!");

            Loyalty.plugin.getServer().getPluginManager().disablePlugin(Loyalty.plugin);
        } finally {
            tryClose(resultSet);
            tryClose(statement);
            tryClose(createStatement);
            tryClose(connection);
        }
    }

    private Connection getConnection (PoolIdentifier identifier) throws SQLException {
        Connection connection = null;

        switch (identifier) {
            case LOAD:
                connection = loadPool.getConnection();
                break;
            case MISC:
                connection = miscPool.getConnection();
                break;
            case SAVE:
                connection = savePool.getConnection();
                break;
        }
        if (connection == null) {
            throw new RuntimeException("getConnection() for " + identifier.name().toLowerCase() + " pool timed out.  Increase max connections settings.");
        }
        return connection;
    }

    private PlayerProfile loadFromResult (String playerName, ResultSet result) {
        UUID uuid;
        int points;

        try {
            uuid = UUID.fromString(result.getString(1));
        } catch (Exception e) {
            uuid = null;
        }

        try {
            points = result.getInt(2);
        } catch (Exception e) {
            points = 0;
        }

        return new PlayerProfile(playerName, uuid, points, true);
    }

    private void printErrors (SQLException e) {
        StackTraceElement element = e.getStackTrace()[0];
        Loyalty.plugin.getLogger().severe("Location: " + element.getClassName() + " " + element.getMethodName() + " " + element.getLineNumber());
        Loyalty.plugin.getLogger().severe("SQLException: " + e.getMessage());
        Loyalty.plugin.getLogger().severe("SQLState: " + e.getSQLState());
        Loyalty.plugin.getLogger().severe("VendorError: " + e.getErrorCode());
    }

    private void tryClose (AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
    }

    private int getUserID (final Connection connection, final String playerName, final UUID uuid) {
        if (uuid == null) {
            return getUserIDByName(connection, playerName);
        }

        if (cachedUserIDs.containsKey(uuid)) {
            return cachedUserIDs.get(uuid);
        }

        ResultSet resultSet = null;
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("SELECT id, user FROM loyalty_users WHERE uuid = ? OR (uuid IS NULL AND user = ?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, playerName);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");

                cachedUserIDs.put(uuid, id);

                return id;
            }
        } catch (SQLException e) {
            printErrors(e);
        } finally {
            tryClose(resultSet);
            tryClose(statement);
        }

        return -1;
    }

    private int getUserIDByName (final Connection connection, final String playerName) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement("SELECT id, user FROM loyalty_users WHERE user = ?");
            statement.setString(1, playerName);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            printErrors(e);
        } finally {
            tryClose(resultSet);
            tryClose(statement);
        }

        return -1;
    }

    public void onDisable() {
        Loyalty.plugin.debug("Releasing connection pool resources...");
        miscPool.close();
        loadPool.close();
        savePool.close();
    }

    public enum PoolIdentifier {
        MISC,
        LOAD,
        SAVE
    }
}
