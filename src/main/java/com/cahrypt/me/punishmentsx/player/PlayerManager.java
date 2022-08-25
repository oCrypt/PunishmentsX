package com.cahrypt.me.punishmentsx.player;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import dev.fumaz.commons.bukkit.misc.Scheduler;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
            String storableName = SpecificPlayerInfo.CURRENT_USERNAME.getCachedPlayerInfo(player);
            String storableUUID = SpecificPlayerInfo.UNIQUE_IDENTIFICATION.getCachedPlayerInfo(player);
            String storableAddress = SpecificPlayerInfo.IPV4_ADDRESS.getCachedPlayerInfo(player);

            // I hate this... Probably should just make an update query, but it's fine for now
            preparedStatement.setString(1, storableName);
            preparedStatement.setString(2, storableUUID);
            preparedStatement.setString(3, storableAddress);

            preparedStatement.setString(4, storableName);
            preparedStatement.setString(5, storableUUID);
            preparedStatement.setString(6, storableAddress);
        });
    }

    public enum SpecificPlayerInfo {

        CURRENT_USERNAME("recentName", 16) {
            @Override
            public String getCachedPlayerInfo(@NotNull Player player) {
                return player.getName();
            }
        },

        UNIQUE_IDENTIFICATION("uuid", 36) {
            @Override
            public String getCachedPlayerInfo(@NotNull Player player) {
                return player.getUniqueId().toString();
            }
        },

        IPV4_ADDRESS("recentAddress", 16) {
            @Override
            public String getCachedPlayerInfo(@NotNull Player player) {
                return player.getAddress().getAddress().toString();
            }
        };

        private final String columnLabel;
        private final int size;

        SpecificPlayerInfo(String columnLabel, int size) {
            this.columnLabel = columnLabel;
            this.size = size;
        }

        /**
         * Obtain the specific information of a player if they're online
         * @param player the online player
         * @return the specific information
         */
        public abstract String getCachedPlayerInfo(@NotNull Player player);

        /**
         * Obtain the specific information of a player if they're online, given their name
         * @param playerName the online player's name
         * @return the specific information
         */
        public String getCachedPlayerInfo(@NotNull String playerName) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().equals(playerName)) {
                    return getCachedPlayerInfo(player);
                }
            }

            return null;
        }

        /**
         * Use the cached player's specific information
         * @param playerName the online player's name
         * @param specificInfoConsumer the specific information consumer
         * @param elseRunnable the runnable that runs if no cached player was found
         */
        public void useCachedPlayerInfoOrElse(@NotNull String playerName, Consumer<String> specificInfoConsumer, Runnable elseRunnable) {
            String specificInfo = getCachedPlayerInfo(playerName);

            if (specificInfo != null) {
                specificInfoConsumer.accept(specificInfo);
            } else {
                elseRunnable.run();
            }
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
            useSpecificPlayerInfoOrElse(name, playerInfoConsumer, () -> {});
        }

        /**
         * Consume the player's corresponding specific information asynchronously
         *
         * @param name               the name of the player
         * @param playerInfoConsumer the consumer for specific information
         */
        public void useSpecificPlayerInfoAsync(String name, Consumer<String> playerInfoConsumer) {
            useSpecificPlayerInfoOrElseAsync(name, playerInfoConsumer, () -> {});
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
        public void useSpecificPlayerInfoOrElse(String name, Consumer<String> playerInfoConsumer, Runnable elseRunnable) {
            useCachedPlayerInfoOrElse(name, playerInfoConsumer, () ->
                    HIKARI_DATABASE.executeResultQuery(PlayerSQLStatements.FETCH_PLAYER_INFO,
                            preparedStatement -> preparedStatement.setString(1, name),
                            resultSet -> {
                                if (resultSet.next()) {
                                    playerInfoConsumer.accept(resultSet.getString(columnLabel));
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
        public void useSpecificPlayerInfoOrElseAsync(String name, Consumer<String> playerInfoConsumer, Runnable elseRunnable) {
            useCachedPlayerInfoOrElse(name, playerInfoConsumer, () ->
                    HIKARI_DATABASE.executeResultQueryASync(PlayerSQLStatements.FETCH_PLAYER_INFO,
                            preparedStatement -> preparedStatement.setString(1, name),
                            resultSet -> {
                                String specificInfo = resultSet.getString(columnLabel);
                                SCHEDULER.runTask((resultSet.next() ? () -> playerInfoConsumer.accept(specificInfo) : elseRunnable));
                    }));
        }
    }
}
