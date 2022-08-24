package com.cahrypt.me.punishmentsx.player;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import dev.fumaz.commons.bukkit.misc.Scheduler;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.function.Consumer;

public class PlayerManager {
    private static final HikariDatabase HIKARI_DATABASE = DataSource.getHikariDatabase();
    private static final Scheduler SCHEDULER = Scheduler.of(PunishmentsX.class);

    public PlayerManager() {
        HIKARI_DATABASE.executeQueryASync(PlayerSQLStatements.CREATE_PLAYER_INFO_TABLE);
    }

    /**
     * Logs the specified player's information in the SQL database (name, UUID, IPv4)
     *
     * @param player the player to be logged
     */
    public void logPlayer(Player player) {
        HIKARI_DATABASE.executeQueryASync(PlayerSQLStatements.INSERT_NEW_PLAYER, preparedStatement -> {
            // I hate this... Probably should just make an update query, but it's fine for now
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.setString(3, player.getAddress().getAddress().toString());

            preparedStatement.setString(4, player.getName());
            preparedStatement.setString(5, player.getUniqueId().toString());
            preparedStatement.setString(6, player.getAddress().getAddress().toString());
        });
    }

    public enum SpecificPlayerInfo {

        CURRENT_USERNAME("recentName", 16),
        UNIQUE_IDENTIFICATION("uuid", 36),
        IPV4_ADDRESS("recentAddress", 16);

        private final String columnLabel;
        private final int size;

        SpecificPlayerInfo(String columnLabel, int size) {
            this.columnLabel = columnLabel;
            this.size = size;
        }

        public int getSize() {
            return size;
        }

        /**
         * Consume the player's corresponding specific information
         *
         * @param name               the name of the player
         * @param playerInfoConsumer the consumer for specific information
         */
        public void useSpecificPlayerInfo(String name, Consumer<String> playerInfoConsumer) {
            useSpecificPlayerInfoOrElse(name, playerInfoConsumer, str -> {
            });
        }

        /**
         * Consume the player's corresponding specific information asynchronously
         *
         * @param name               the name of the player
         * @param playerInfoConsumer the consumer for specific information
         */
        public void useSpecificPlayerInfoAsync(String name, Consumer<String> playerInfoConsumer) {
            useSpecificPlayerInfoOrElseAsync(name, playerInfoConsumer, str -> {
            });
        }

        /**
         * Consume the player's corresponding specific information
         *
         * @param name               the name of the player
         * @param playerInfoConsumer the consumer for specific information
         * @param elseConsumer the name consumer that runs if no player information is found
         */
        public void useSpecificPlayerInfoOrElse(String name, Consumer<String> playerInfoConsumer, Consumer<String> elseConsumer) {
            HIKARI_DATABASE.executeResultQuery(PlayerSQLStatements.FETCH_PLAYER_INFO, preparedStatement -> preparedStatement.setString(1, name), resultSet -> {
                if (resultSet.next()) {
                    playerInfoConsumer.accept(resultSet.getString(columnLabel));
                } else {
                    elseConsumer.accept(name);
                }
            });
        }

        /**
         * Consume the player's corresponding specific information asynchronously
         *
         * @param name               the name of the player
         * @param playerInfoConsumer the consumer for specific information
         * @param elseConsumer the name consumer that runs if no player information is found
         */
        public void useSpecificPlayerInfoOrElseAsync(String name, Consumer<String> playerInfoConsumer, Consumer<String> elseConsumer) {
            HIKARI_DATABASE.executeResultQueryASync(PlayerSQLStatements.FETCH_PLAYER_INFO, preparedStatement -> preparedStatement.setString(1, name), resultSet -> {
                if (resultSet.next()) {
                    SCHEDULER.runTask(task -> {
                        try {
                            playerInfoConsumer.accept(resultSet.getString(columnLabel));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    elseConsumer.accept(name);
                }
            });
        }
    }
}
