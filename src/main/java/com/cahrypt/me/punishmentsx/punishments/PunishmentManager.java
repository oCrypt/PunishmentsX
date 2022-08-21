package com.cahrypt.me.punishmentsx.punishments;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
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
    private final Logger logger;
    private final Map<PunishmentStorage, ActivePunishmentListener> punishmentListenerMap;
    private static final Scheduler scheduler = Scheduler.of(PunishmentsX.class);

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
                new TempMuteHandler(), new MuteHandler()
        ) {

            @Override
            public String getStorableTarget(StorablePlayerInfo targetInfo) {
                return targetInfo.getUUID();
            }
        },

        KICK_STORAGE(
                "kicklogs",
                new KickHandler()
        ) {

            @Override
            public String getStorableTarget(StorablePlayerInfo targetInfo) {
                return targetInfo.getUUID();
            }
        },

        BAN_STORAGE(
                "banlogs",
                new TempBanHandler(), new BanHandler()
        ) {

            @Override
            public String getStorableTarget(StorablePlayerInfo targetInfo) {
                return targetInfo.getUUID();
            }
        },

        BLACKLIST_STORAGE(
                "blacklistlogs",
                new BlacklistHandler()
        ) {

            @Override
            public String getStorableTarget(StorablePlayerInfo targetInfo) {
                return targetInfo.getAddress();
            }
        };

        private final PlayerManager playerManager;
        private final HikariDatabase database;

        private final PunishmentSQLStatements storageStatements;
        private final Set<PunishmentHandler> handlerSet;

        /**
         * Create a new SQL storage for which punishments can be stored in
         * @param tableName the name of the SQL table
         * @param handlers the punishment handlers sharing the storage
         */
        PunishmentStorage(@NotNull String tableName, @NotNull PunishmentHandler... handlers) {
            this.playerManager = JavaPlugin.getPlugin(PunishmentsX.class).getPlayerManager();
            this.database = DataSource.getHikariDatabase();

            this.storageStatements = new PunishmentSQLStatements(tableName);
            this.handlerSet = Set.of(handlers);

            handlerSet.forEach(handler -> handler.registerStorage(this));
            database.executeQueryASync(storageStatements.getPunishmentTableQuery());
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
         * Use the specified {@link StorablePlayerInfo} to convert the target into the appropriate SQL-storable string
         * @param targetInfo the {@link StorablePlayerInfo}
         * @return the SQL-storable string
         */
        public abstract String getStorableTarget(StorablePlayerInfo targetInfo);

        /**
         * Get the list of punishment handlers that share the specified storage
         * @return list of sharing punishment handlers
         */
        public Set<PunishmentHandler> getSharingHandlers() {
            return handlerSet;
        }

        /**
         * Consumes the target's active punishments, given their {@link StorablePlayerInfo}
         * @param playerInfo the player's {@link StorablePlayerInfo}
         * @param punishmentConsumer the consumer for the active punishments
         */
        public void consumeActivePunishmentInfo(StorablePlayerInfo playerInfo, Consumer<PunishmentInfo> punishmentConsumer) {
            database.executeResultQuery(storageStatements.getActivePunishmentsQuery(),
                    preparedStatement -> {
                        preparedStatement.setString(1, getStorableTarget(playerInfo));
                        preparedStatement.setTimestamp(2, new Timestamp(Utils.getCurrentTimeMillis()));
                    }, resultSet -> {
                        if (resultSet.next()) {
                            punishmentConsumer.accept(new PunishmentInfo(resultSet));
                        }
                    });
        }

        /**
         * Consumes the target's active punishments, given their name
         * @param name the player's name
         * @param punishmentConsumer the consumer for the active punishments
         */
        public void consumeActivePunishmentInfo(String name, Consumer<PunishmentInfo> punishmentConsumer) {
            playerManager.usePlayerInfoOrElse(name, info -> consumeActivePunishmentInfo(info, punishmentConsumer), str -> {});
        }

        /**
         * Consumes the target's active punishments asynchronously, given their name
         * @param name the player's name
         * @param punishmentConsumer the consumer for the active punishments, executed synchronously
         */
        public void consumeActivePunishmentInfoASync(String name, Consumer<PunishmentInfo> punishmentConsumer) {
            playerManager.usePlayerInfoOrElseASync(name, info -> consumeActivePunishmentInfo(info, punishmentConsumer), str -> {});
        }

        /**
         * Consumes the target's punishments asynchronously, given their {@link StorablePlayerInfo}
         * Only offered asynchronously, as the database queries are costly
         * @param playerInfo the player's {@link StorablePlayerInfo}
         * @param punishmentConsumer the consumer for the punishments, executed synchronously
         */
        public void consumePunishmentInfoASync(StorablePlayerInfo playerInfo, Consumer<List<PunishmentInfo>> punishmentConsumer) {
            playerManager.usePlayerInfoOrElseASync(playerInfo.getName(), info -> database.executeResultQueryASync(storageStatements.getPunishmentsQuery(),
                    preparedStatement -> preparedStatement.setString(1, getStorableTarget(playerInfo)), resultSet -> {
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

                            scheduler.runTask(task -> punishmentConsumer.accept(punishmentInfoList));
                    }), str -> {});
        }

        /**
         * Asynchronously logs the given {@link PunishmentInfo} to the SQL database
         * @param punishmentInfo the {@link PunishmentInfo} to be logged
         */
        public void logPlaceASync(@NotNull PunishmentInfo punishmentInfo) {
            scheduler.runTaskAsynchronously(task -> logPlace(punishmentInfo));
        }

        /**
         * Asynchronously pardons all active punishments of the specified target
         * @param sender the pardoner
         * @param targetInfo the target's {@link StorablePlayerInfo}
         * @param reason the pardon reason
         */
        public void logPardonASync(@NotNull CommandSender sender, @NotNull StorablePlayerInfo targetInfo, @NotNull String reason) {
            scheduler.runTaskAsynchronously(task -> logPardon(sender, targetInfo, reason));
        }

        /**
         * Synchronously logs the given {@link PunishmentInfo} to the SQL database
         * @param punishmentInfo the {@link PunishmentInfo} to be logged
         */
        public void logPlace(@NotNull PunishmentInfo punishmentInfo) {
            database.executeQuery(
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
         * Synchronously pardons all active punishments of the specified target
         * @param sender the pardoner
         * @param targetInfo the target's {@link StorablePlayerInfo}
         * @param reason the pardon reason
         */
        public void logPardon(@NotNull CommandSender sender, @NotNull StorablePlayerInfo targetInfo, @NotNull String reason) {
            database.executeQuery(
                    storageStatements.getPunishmentPardonQuery(),
                    preparedStatement -> {
                        preparedStatement.setString(1, getStorableSender(sender));
                        preparedStatement.setString(2, reason);
                        preparedStatement.setString(3, getStorableTarget(targetInfo));
                        preparedStatement.setTimestamp(4, new Timestamp(Utils.getCurrentTimeMillis()));
                    }
            );
        }
    }
}
