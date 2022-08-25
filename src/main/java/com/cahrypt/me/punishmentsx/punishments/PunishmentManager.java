package com.cahrypt.me.punishmentsx.punishments;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.punishments.handler.PunishmentHandler;
import com.cahrypt.me.punishmentsx.punishments.handler.perm.BanHandler;
import com.cahrypt.me.punishmentsx.punishments.handler.perm.MuteHandler;
import com.cahrypt.me.punishmentsx.punishments.handler.temp.TempBanHandler;
import com.cahrypt.me.punishmentsx.punishments.handler.perm.BlacklistHandler;
import com.cahrypt.me.punishmentsx.punishments.handler.perm.KickHandler;
import com.cahrypt.me.punishmentsx.punishments.handler.temp.TempMuteHandler;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.misc.Scheduler;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class PunishmentManager {
    private static final HikariDatabase HIKARI_DATABASE = DataSource.getHikariDatabase();
    private static final PlayerManager PLAYER_MANAGER = JavaPlugin.getPlugin(PunishmentsX.class).getPlayerManager();
    private static final Scheduler SCHEDULER = Scheduler.of(PunishmentsX.class);

    private final Logger logger;
    private final Map<PunishmentStorage, ActivePunishmentListener> punishmentListenerMap;

    public PunishmentManager() {
        this.logger = Bukkit.getLogger();
        this.punishmentListenerMap = new HashMap<>();
    }

    /**
     * Register the active punishment listener for all punishments sharing the specified storage
     * @param storage the punishment storage
     * @param activePunishmentListener the active punishment listener to register
     */
    protected void registerStorageListener(@NotNull PunishmentStorage storage, @NotNull ActivePunishmentListener activePunishmentListener) {
        punishmentListenerMap.put(storage, activePunishmentListener);
    }

    /**
     * Send a status report to the console of all registered punishment listeners
     */
    public void reportListenerStatus() {
        logger.info("Commencing Punishment Listener Status Check...");

        Arrays.stream(PunishmentStorage.values())
                .forEach(punishment -> {
                    if (punishmentListenerMap.containsKey(punishment)) {
                        logger.info(punishment.toString() + " -> (" + punishmentListenerMap.get(punishment) + ") Storage listener registered and standing by...");
                    } else {
                        logger.warning(punishment.toString() + " -> Storage Listener not registered...");
                    }
                });
    }

    /**
     * Unregisters the active punishment listener associated with the given storage if any
     * @param storage the storage to unregister
     */
    public void unregisterPunishmentListener(@NotNull PunishmentStorage storage) {
        ActivePunishmentListener activePunishmentListener = punishmentListenerMap.get(storage);

        if (activePunishmentListener == null) {
            logger.warning("No listener of storage type " + storage + " to unregister!");
            return;
        }

        punishmentListenerMap.get(storage).unregister();
        Bukkit.getLogger().info(storage + " listener unregistered... If this wasn't intentional, please check source code");
        punishmentListenerMap.remove(storage);
    }

    /**
     * Unregisters all active punishment listeners
     */
    public void unregisterAll() {
        punishmentListenerMap.keySet().forEach(this::unregisterPunishmentListener);
    }

    public enum PunishmentStorage {

        MUTE_STORAGE(
                "mutelogs",
                PlayerManager.SpecificPlayerInfo.UNIQUE_IDENTIFICATION,
                new TempMuteHandler(), new MuteHandler()
        ),

        KICK_STORAGE(
                "kicklogs",
                PlayerManager.SpecificPlayerInfo.UNIQUE_IDENTIFICATION,
                new KickHandler()
        ),

        BAN_STORAGE(
                "banlogs",
                PlayerManager.SpecificPlayerInfo.UNIQUE_IDENTIFICATION,
                new TempBanHandler(), new BanHandler()
        ),

        BLACKLIST_STORAGE(
                "blacklistlogs",
                PlayerManager.SpecificPlayerInfo.IPV4_ADDRESS,
                new BlacklistHandler()
        );

        private final PunishmentSQLStatements storageStatements;
        private final PlayerManager.SpecificPlayerInfo specificPlayerInfo;
        private final Set<PunishmentHandler> handlerSet;

        /**
         * Create a new SQL storage for which punishments can be stored in
         * @param tableName the name of the SQL table
         * @param handlers the punishment handlers sharing the storage
         */
        PunishmentStorage(@NotNull String tableName, @NotNull PlayerManager.SpecificPlayerInfo specificPlayerInfo, @NotNull PunishmentHandler... handlers) {
            this.storageStatements = new PunishmentSQLStatements(tableName);
            this.specificPlayerInfo = specificPlayerInfo;
            this.handlerSet = Set.of(handlers);

            handlerSet.forEach(handler -> handler.registerStorage(this));
            HIKARI_DATABASE.executeQueryASync(storageStatements.getPunishmentTableQuery(specificPlayerInfo.getSize()));
        }

        public PlayerManager.SpecificPlayerInfo getStorableTargetType() {
            return specificPlayerInfo;
        }

        /**
         * Convert the {@link CommandSender} into an SQL-storable string
         * @param sender the {@link CommandSender}
         * @return the SQL-storable string
         */
        public String getStorableSender(CommandSender sender) {
            return (sender instanceof Player ? ((Player) sender).getUniqueId().toString() : sender.getName());
        }

        /**
         * Get the list of punishment handlers that share the specified storage
         * @return list of sharing punishment handlers
         */
        public Set<PunishmentHandler> getSharingHandlers() {
            return handlerSet;
        }

        /**
         * Consumes the target's active punishments, given their appropriate storable form
         * @param storableTarget the target's appropriate storable form
         * @param punishmentConsumer the consumer for the active punishments
         */
        public void consumeActivePunishmentInfoExplicitly(String storableTarget, Consumer<PunishmentInfo> punishmentConsumer) {
            HIKARI_DATABASE.executeResultQuery(storageStatements.getActivePunishmentsQuery(),
                    preparedStatement -> {
                        preparedStatement.setString(1, storableTarget);
                        preparedStatement.setTimestamp(2, new Timestamp(Utils.getCurrentTimeMillis()));
                    }, resultSet -> {
                        if (resultSet.next()) {
                            punishmentConsumer.accept(new PunishmentInfo(resultSet));
                        }
                    });
        }

        /**
         * Consumes the target's active punishments, given their name
         * @param playerName the player's name
         * @param punishmentConsumer the consumer for the active punishments
         */
        public void consumeActivePunishmentInfo(String playerName, Consumer<PunishmentInfo> punishmentConsumer) {
            specificPlayerInfo.useSpecificPlayerInfo(playerName, storableTarget -> consumeActivePunishmentInfoExplicitly(storableTarget, punishmentConsumer));
        }

        /**
         * Consumes the target's active punishments asynchronously, given their name
         * @param playerName the player's name
         * @param punishmentConsumer the consumer for the active punishments, executed synchronously
         */
        public void consumeActivePunishmentInfoAsync(String playerName, Consumer<PunishmentInfo> punishmentConsumer) {
            specificPlayerInfo.useSpecificPlayerInfoAsync(playerName, info -> consumeActivePunishmentInfo(info, punishmentConsumer));
        }

        /**
         * Consumes the target's punishments asynchronously, given their name
         * Only offered asynchronously, as the database queries are costly
         * @param playerName the player's name
         * @param punishmentConsumer the consumer for the punishments, executed synchronously
         */
        public void consumePunishmentInfoAsync(String playerName, Consumer<List<PunishmentInfo>> punishmentConsumer) {
            specificPlayerInfo.useSpecificPlayerInfoAsync(playerName, storableTarget -> HIKARI_DATABASE.executeResultQueryASync(storageStatements.getPunishmentsQuery(),
                    preparedStatement -> preparedStatement.setString(1, storableTarget), resultSet -> {
                            List<PunishmentInfo> punishmentInfoList;

                            try {
                                punishmentInfoList = new ArrayList<>();

                                while (resultSet.next()) {
                                    punishmentInfoList.add(new PunishmentInfo(resultSet));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                return;
                            }

                            SCHEDULER.runTask(() -> punishmentConsumer.accept(punishmentInfoList));
                    }));
        }

        /**
         * Asynchronously logs the given {@link PunishmentInfo} to the SQL database
         * @param punishmentInfo the {@link PunishmentInfo} to be logged
         */
        public void logPlaceAsync(@NotNull PunishmentInfo punishmentInfo) {
            SCHEDULER.runTaskAsynchronously(() -> logPlace(punishmentInfo));
        }

        /**
         * Synchronously logs the given {@link PunishmentInfo} to the SQL database
         * @param punishmentInfo the {@link PunishmentInfo} to be logged
         */
        public void logPlace(@NotNull PunishmentInfo punishmentInfo) {
            HIKARI_DATABASE.executeQuery(
                    storageStatements.getPunishmentPlaceQuery(),
                    preparedStatement -> {
                        preparedStatement.setString(1, punishmentInfo.getStorableTarget());
                        preparedStatement.setString(2, punishmentInfo.getStorableSender());
                        preparedStatement.setString(3, punishmentInfo.getReason());
                        preparedStatement.setTimestamp(4, punishmentInfo.getPunishDate());
                        preparedStatement.setTimestamp(5, punishmentInfo.getExpiry());
                    }
            );
        }

        /**
         * Asynchronously pardons all active punishments of the specified explicit storable target
         * @param sender the pardoner
         * @param storableTarget the target's storable form
         * @param reason the pardon reason
         */
        public void logExplicitPardonAsync(@NotNull CommandSender sender, @NotNull String storableTarget, @NotNull String reason) {
            SCHEDULER.runTaskAsynchronously(() -> logExplicitPardon(sender, storableTarget, reason));
        }

        /**
         * Synchronously pardons all active punishments of the specified explicit storable target
         * @param sender the pardoner
         * @param storableTarget the target's storable form
         * @param reason the pardon reason
         */
        public void logExplicitPardon(@NotNull CommandSender sender, @NotNull String storableTarget, @NotNull String reason) {
            HIKARI_DATABASE.executeQuery(
                    storageStatements.getPunishmentPardonQuery(),
                    preparedStatement -> {
                        preparedStatement.setString(1, getStorableSender(sender));
                        preparedStatement.setString(2, reason);
                        preparedStatement.setString(3, storableTarget);
                        preparedStatement.setTimestamp(4, new Timestamp(Utils.getCurrentTimeMillis()));
                    }
            );
        }

        /**
         * Asynchronously pardons all active punishments of the specified target
         * @param sender the pardoner
         * @param target the target
         * @param reason the pardon reason
         */
        public void logPardonAsync(@NotNull CommandSender sender, @NotNull OfflinePlayer target, @NotNull String reason) {
            SCHEDULER.runTaskAsynchronously(() -> logPardon(sender, target, reason));
        }

        /**
         * Synchronously pardons all active punishments of the specified target
         * @param sender the pardoner
         * @param target the target
         * @param reason the pardon reason
         */
        public void logPardon(@NotNull CommandSender sender, @NotNull OfflinePlayer target, @NotNull String reason) {
            specificPlayerInfo.useSpecificPlayerInfo(target.getName(), storableTarget -> logExplicitPardon(sender, storableTarget, reason));
        }
    }
}
