package com.cahrypt.me.punishmentsx.punishments;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class PunishmentHandler implements CommandExecutor {
    protected final PluginCommand punishmentCommand;
    private final int playerCmdIndex;
    private final int reasonCmdIndex;

    protected final HikariDatabase database;
    protected final PlayerManager playerManager;

    protected PunishmentManager.PunishmentStorage storage;

    /**
     * Registers a new punishment that will be handled via this instance
     * @param punishmentCommand the command used to run the punishment
     */
    public PunishmentHandler(String punishmentCommand) {
        PunishmentsX main = JavaPlugin.getPlugin(PunishmentsX.class);

        this.punishmentCommand = main.getCommand(punishmentCommand);
        this.playerCmdIndex = getUsageIndex("player");
        this.reasonCmdIndex = getUsageIndex("reason");

        this.database = DataSource.getHikariDatabase();
        this.playerManager = main.getPlayerManager();

        main.getCommand(punishmentCommand).setExecutor(this);
    }

    /**
     * Sets the punishment's storage
     * @param storage the storage
     */
    protected void registerStorage(PunishmentManager.PunishmentStorage storage) {
        this.storage = storage;
    }

    /**
     * Obtains the argument index the specified string is located in
     * @param string the string to search for
     */
    protected int getUsageIndex(String string) throws RuntimeException {
        String[] usageArgs = punishmentCommand.getUsage().split(" ");

        for (int i = 0; i < usageArgs.length; i++) {
            if (usageArgs[i].contains(string)) {
                return i - 1;
            }
        }

        throw new RuntimeException(punishmentCommand.getName() + "punishment contains invalid time index");
    }

    /**
     * Whether the command should proceed with execution or be terminated
     * @param sender the command sender
     * @param rawArgs the raw command arguments
     * @param targetInfo the target's SQL-storable information
     * @param punishmentInfo the SQL-storable punishment information
     * @return whether the command should proceed with execution
     */
    protected abstract boolean validatePunishment(@NotNull CommandSender sender, @NotNull String[] rawArgs, @NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo);

    /**
     * Specify what should be done when the punishment is placed
     * @param targetInfo the SQL-storable information of the target
     * @param punishmentInfo the SQL-storable punishment information
     */
    protected abstract void onPunishmentPlace(@NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission(Utils.PUNISHMENT_ADMIN_PERMISSION + "." + command.getName())) {
            sender.sendMessage(Utils.ERROR_PREFIX + "You do not have valid permissions to execute that command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Utils.INVALID_USAGE + "Not enough arguments! Please use " + command.getUsage());
            return true;
        }

        playerManager.usePlayerInfoOrElseASync(
                args[playerCmdIndex],
                playerInfo -> {
                    String reason = Utils.concatArray(args, " ", reasonCmdIndex);
                    PunishmentInfo punishmentInfo = new PunishmentInfo(storage.getStorableTarget(playerInfo), storage.getStorableSender(sender), reason);

                    if (!validatePunishment(sender, args, playerInfo, punishmentInfo)) {
                        return;
                    }

                    storage.logPardon(sender, playerInfo, "Overridden Punishment");
                    storage.logPlaceASync(punishmentInfo);
                    onPunishmentPlace(playerInfo, punishmentInfo);

                    String targetName = playerInfo.getName();
                    String punishmentCommandName = punishmentCommand.getName();

                    sender.sendMessage(Utils.SUCCESS_PREFIX + targetName + " has successfully received a " + punishmentCommandName);
                    Bukkit.getOnlinePlayers().forEach(player ->
                            player.sendMessage(ChatColor.RED + playerInfo.getName() + ChatColor.GRAY + " has received a " + punishmentCommand.getName() + " from " + ChatColor.RED + sender.getName() + ChatColor.GRAY + (player.hasPermission(Utils.PUNISHMENT_ADMIN_PERMISSION) ? " for " + reason : "!"))
                    );
                },
                name -> sender.sendMessage(Utils.INVALID_USAGE + "'" + name + "' has not joined before or is an invalid player!")
        );

        return true;
    }
}
