package com.cahrypt.me.punishmentsx.player;

import com.cahrypt.me.punishmentsx.storage.DataSource;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class StorablePlayerInfo {
    private final HikariDatabase hikariDatabase;

    private final String playerName;
    private final String uuid;
    private final String address;

    private StorablePlayerInfo(@NotNull String playerName, @NotNull String uuid, @NotNull String address) {
        this.hikariDatabase = DataSource.getHikariDatabase();

        this.playerName = playerName;
        this.uuid = uuid;
        this.address = address;
    }

    public static StorablePlayerInfo from(@NotNull Player player) {
        return new StorablePlayerInfo(player.getName(), player.getUniqueId().toString(), player.getAddress().getAddress().toString());
    }

    public static StorablePlayerInfo from(@NotNull ResultSet resultSet) throws SQLException {
        return new StorablePlayerInfo(resultSet.getString("recentName"), resultSet.getString("uuid"), resultSet.getString("recentAddress"));
    }

    /**
     * Logs the player's information in the SQL database (name, UUID, IPv4)
     */
    public void log() {
        hikariDatabase.executeQueryASync(PlayerSQLStatements.INSERT_NEW_PLAYER, preparedStatement -> {
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, uuid);
            preparedStatement.setString(3, address);

            preparedStatement.setString(4, playerName);
            preparedStatement.setString(5, uuid);
            preparedStatement.setString(6, address);
        });
    }

    public String getName() {
        return playerName;
    }

    public String getUUID() {
        return uuid;
    }

    public String getAddress() {
        return address;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(UUID.fromString(uuid));
    }

}
