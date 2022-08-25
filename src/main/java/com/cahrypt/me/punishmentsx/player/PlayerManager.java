package com.cahrypt.me.punishmentsx.player;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import dev.fumaz.commons.bukkit.misc.Scheduler;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class PlayerManager {
    private final HikariDatabase hikariDatabase;
    private final Scheduler scheduler;
    private final Map<String, StorablePlayerInfo> cachedPlayerInfo;

    public PlayerManager() {
        this.hikariDatabase = DataSource.getHikariDatabase();
        this.scheduler = Scheduler.of(PunishmentsX.class);
        this.cachedPlayerInfo = new HashMap<>();

        hikariDatabase.executeQueryASync(PlayerSQLStatements.CREATE_PLAYER_INFO_TABLE);
    }

    public void cachePlayerInfo(StorablePlayerInfo playerInfo) {
        cachedPlayerInfo.put(playerInfo.getName(), playerInfo);
    }

    public void removePlayerInfo(String playerName) {
        cachedPlayerInfo.remove(playerName);
    }

    public void useCachedPlayerInfoOrElse(String playerName, Consumer<StorablePlayerInfo> playerInfoConsumer, Runnable elseRunnable) {
        if (cachedPlayerInfo.containsKey(playerName)) {
            playerInfoConsumer.accept(cachedPlayerInfo.get(playerName));
            return;
        }

        elseRunnable.run();
    }

    /**
     * Consume the player's corresponding specific information
     *
     * @param name               the name of the player
     * @param playerInfoConsumer the consumer for specific information
     */
    public void usePlayerInfo(String name, Consumer<StorablePlayerInfo> playerInfoConsumer) {
        usePlayerInfoOrElse(name, playerInfoConsumer, () -> {});
    }

    /**
     * Consume the player's corresponding specific information asynchronously
     *
     * @param name               the name of the player
     * @param playerInfoConsumer the consumer for specific information
     */
    public void usePlayerInfoAsync(String name, Consumer<StorablePlayerInfo> playerInfoConsumer) {
        usePlayerInfoOrElseAsync(name, playerInfoConsumer, () -> {});
    }

    /**
     * Consume the player's corresponding specific information
     * First runs a synchronous online player cache check for efficiency reasons
     * If no online player is found, a query is made to the SQL database
     *
     * @param name               the name of the player
     * @param playerInfoConsumer the consumer for specific information
     * @param elseRunnable the runnable that runs if no player information is found
     */
    public void usePlayerInfoOrElse(String name, Consumer<StorablePlayerInfo> playerInfoConsumer, Runnable elseRunnable) {
        useCachedPlayerInfoOrElse(name, playerInfoConsumer, () ->
                hikariDatabase.executeResultQuery(PlayerSQLStatements.FETCH_PLAYER_INFO,
                        preparedStatement -> preparedStatement.setString(1, name),
                        resultSet -> {
                            if (resultSet.next()) {
                                playerInfoConsumer.accept(StorablePlayerInfo.from(resultSet));
                            } else {
                                elseRunnable.run();
                            }
                        }));
    }

    /**
     * Consume the player's corresponding specific information
     * First runs a synchronous online player cache check for efficiency reasons
     * If no online player is found, an asynchronous query is made to the SQL database
     *
     * @param name               the name of the player
     * @param playerInfoConsumer the consumer for specific information
     * @param elseRunnable the runnable that runs if no player information is found
     */
    public void usePlayerInfoOrElseAsync(String name, Consumer<StorablePlayerInfo> playerInfoConsumer, Runnable elseRunnable) {
        useCachedPlayerInfoOrElse(name, playerInfoConsumer, () ->
                hikariDatabase.executeResultQueryASync(PlayerSQLStatements.FETCH_PLAYER_INFO,
                        preparedStatement -> preparedStatement.setString(1, name),
                        resultSet -> {
                            StorablePlayerInfo storablePlayerInfo = StorablePlayerInfo.from(resultSet);
                            scheduler.runTask((resultSet.next() ? () -> playerInfoConsumer.accept(storablePlayerInfo) : elseRunnable));
                        }));
    }
}
