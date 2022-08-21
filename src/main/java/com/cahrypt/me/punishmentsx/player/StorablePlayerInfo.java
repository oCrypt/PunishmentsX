package com.cahrypt.me.punishmentsx.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class StorablePlayerInfo {
    private final String name;
    private final String uuid;
    private final String address;

    private StorablePlayerInfo(String name, String uuid, String address) {
        this.name = name;
        this.uuid = uuid;
        this.address = address;
    }

    /**
     * Obtain SQL-storable target information from the specified {@link ResultSet}
     * @param resultSet the {@link ResultSet}
     * @return the SQL-storable target information
     * @throws SQLException if the {@link ResultSet} is not appropriate to fetch player information from
     */
    public static StorablePlayerInfo fromResultSet(ResultSet resultSet) throws SQLException {
        return new StorablePlayerInfo(resultSet.getString("recentName"), resultSet.getString("uuid"), resultSet.getString("recentAddress"));
    }

    /**
     * Get the current name of the target
     * @return the target name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the unique ID of the target
     * @return the target UUID
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Get the current IP address of the target
     * @return the current IPv4
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get an {@link OfflinePlayer} instance
     * @return an {@link OfflinePlayer} instance
     */
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(uuid));
    }
}
