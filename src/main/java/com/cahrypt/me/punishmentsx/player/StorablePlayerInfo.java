package com.cahrypt.me.punishmentsx.player;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import dev.fumaz.commons.bukkit.misc.Scheduler;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class StorablePlayerInfo {
    private final HikariDatabase hikariDatabase;
    private final Scheduler scheduler;

    private final String playerName;
    private final String uuid;
    private final String address;

    private final BukkitTask evictionTask;

    private StorablePlayerInfo(@NotNull String playerName, @NotNull String uuid, @NotNull String address, @Nullable BukkitTask evictionTask) {
        this.hikariDatabase = DataSource.getHikariDatabase();
        this.scheduler = Scheduler.of(PunishmentsX.class);

        this.playerName = playerName;
        this.uuid = uuid;
        this.address = address;

        this.evictionTask = evictionTask;
    }

    public static StorablePlayerInfo from(@NotNull Player player) {
        return new StorablePlayerInfo(player.getName(), player.getUniqueId().toString(), player.getAddress().getAddress().toString(), null);
    }

    public static StorablePlayerInfo from(@NotNull ResultSet resultSet) throws SQLException {
        return new StorablePlayerInfo(resultSet.getString("recentName"), resultSet.getString("uuid"), resultSet.getString("recentAddress"), null);
    }

    public static StorablePlayerInfo from(@NotNull AsyncPlayerPreLoginEvent event, @NotNull BukkitTask evictionTask) {
        return new StorablePlayerInfo(event.getName(), event.getUniqueId().toString(), event.getAddress().toString(), evictionTask);
    }

    /**
     * Logs the player's information in the SQL database asynchronously (name, UUID, IPv4)
     */
    public void logAsync() {
        scheduler.runTaskAsynchronously(this::log);
    }

    /**
     * Logs the player's information in the SQL database (name, UUID, IPv4)
     */
    public void log() {
        hikariDatabase.executeQuery(PlayerSQLStatements.INSERT_NEW_PLAYER, preparedStatement -> {
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, uuid);
            preparedStatement.setString(3, address);
        });
    }

    public void cancelEvictionTask() {
        evictionTask.cancel();
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
