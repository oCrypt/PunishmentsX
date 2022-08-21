package com.cahrypt.me.punishmentsx.player;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import dev.fumaz.commons.bukkit.misc.Scheduler;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class PlayerManager {
    private final HikariDatabase database;
    private final Scheduler scheduler;

    public PlayerManager() {
        this.database = DataSource.getHikariDatabase();
        this.scheduler = Scheduler.of(PunishmentsX.class);
        database.executeQueryASync(PlayerSQLStatements.CREATE_PLAYER_INFO_TABLE);
    }

    /**
     * Logs the specified player's information in the SQL database (name, UUID, IPv4)
     * @param player the player to be logged
     */
    public void logPlayer(Player player) {
        database.executeQueryASync(PlayerSQLStatements.INSERT_NEW_PLAYER, preparedStatement -> {
            // I hate this... Probably should just make an update query, but it's fine for now
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.setString(3, player.getAddress().getAddress().toString());

            preparedStatement.setString(4, player.getName());
            preparedStatement.setString(5, player.getUniqueId().toString());
            preparedStatement.setString(6, player.getAddress().getAddress().toString());
        });
    }

    /**
     * Consume the player's {@link StorablePlayerInfo} given a name
     * @param name the name of the player
     * @param playerInfoConsumer the consumer for {@link StorablePlayerInfo}
     * @param elseConsumer the name consumer that runs if no player information is found
     */
    public void usePlayerInfoOrElse(String name, Consumer<StorablePlayerInfo> playerInfoConsumer, Consumer<String> elseConsumer) {
        database.executeResultQuery(PlayerSQLStatements.FETCH_PLAYER_INFO, preparedStatement -> preparedStatement.setString(1, name), resultSet -> {
            if (resultSet.next()) {
                playerInfoConsumer.accept(StorablePlayerInfo.fromResultSet(resultSet));
            } else {
                elseConsumer.accept(name);
            }
        });
    }

    /**
     * Asynchronously consume the player's {@link StorablePlayerInfo} given a name
     * @param name the name of the player
     * @param playerInfoConsumer the consumer for {@link StorablePlayerInfo}, executed synchronously
     * @param elseConsumer the name consumer that runs if no player information is found
     */
    public void usePlayerInfoOrElseASync(String name, Consumer<StorablePlayerInfo> playerInfoConsumer, Consumer<String> elseConsumer) {
        database.executeResultQueryASync(PlayerSQLStatements.FETCH_PLAYER_INFO, preparedStatement -> preparedStatement.setString(1, name), resultSet -> {
            if (resultSet.next()) {
                StorablePlayerInfo info = StorablePlayerInfo.fromResultSet(resultSet);
                scheduler.runTask(task -> playerInfoConsumer.accept(info));
            } else {
                elseConsumer.accept(name);
            }
        });
    }
}
