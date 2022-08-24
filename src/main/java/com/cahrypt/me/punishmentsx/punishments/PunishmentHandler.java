package com.cahrypt.me.punishmentsx.punishments;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.command.VoidPunishmentCmd;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.command.CmdArguments;
import dev.fumaz.commons.bukkit.command.annotation.SubCommand;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PunishmentHandler extends VoidPunishmentCmd {
    protected final PluginCommand punishmentCommand;
    protected final HikariDatabase database;
    protected final PlayerManager playerManager;

    protected PunishmentManager.PunishmentStorage storage;

    /**
     * Registers a new punishment that will be handled via this instance
     * @param punishmentCommand the command used to run the punishment
     */
    public PunishmentHandler(String punishmentCommand) {
        super(PunishmentsX.class, punishmentCommand);
        PunishmentsX main = JavaPlugin.getPlugin(PunishmentsX.class);

        this.punishmentCommand = main.getCommand(punishmentCommand);

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
     * Whether the command should proceed with execution or be terminated
     * @param sender the command sender
     * @param rawArgs the raw command arguments
     * @param target the target
     * @param punishmentInfo the SQL-storable punishment information
     * @return whether the command should proceed with execution
     */
    protected abstract boolean validatePunishment(@NotNull CommandSender sender, @NotNull String[] rawArgs, @NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo);

    /**
     * Specify what should be done when the punishment is placed
     * @param target the target
     * @param punishmentInfo the SQL-storable punishment information
     */
    protected abstract void onPunishmentPlace(@NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo);

    @SubCommand(cmdArgStructure = { CmdArguments.CmdArgumentTypes.OFFLINE_PLAYER, CmdArguments.CmdArgumentTypes.STRING }, permission = Utils.PUNISHMENT_ADMIN_PERMISSION, exactMatch = false)
    public void onPunishCommand(@NotNull CommandSender sender, @NotNull List<Object> usefulArgs, String[] rawArgs) {
        OfflinePlayer target = (OfflinePlayer) usefulArgs.get(0);
        String targetName = target.getName();

        storage.getStorableTargetType().useSpecificPlayerInfoOrElseAsync(
                targetName,
                storableTarget -> {
                    String reason = Utils.concatArray(rawArgs, " ", usefulArgs.size());
                    PunishmentInfo punishmentInfo = new PunishmentInfo(storableTarget, storage.getStorableSender(sender), reason);

                    if (!validatePunishment(sender, rawArgs, target, punishmentInfo)) {
                        return;
                    }

                    storage.logExplicitPardon(sender, storableTarget, "Overridden Punishment");
                    storage.logPlaceAsync(punishmentInfo);
                    onPunishmentPlace(target, punishmentInfo);

                    String punishmentCommandName = punishmentCommand.getName();

                    sender.sendMessage(Utils.SUCCESS_PREFIX + targetName + " has successfully received a " + punishmentCommandName);
                    Bukkit.getOnlinePlayers().forEach(player ->
                            player.sendMessage(ChatColor.RED + targetName + ChatColor.GRAY + " has received a " + punishmentCommand.getName() + " from " + ChatColor.RED + sender.getName() + ChatColor.GRAY + (player.hasPermission(Utils.PUNISHMENT_ADMIN_PERMISSION) ? " for " + reason : "!"))
                    );
                },
                name -> sender.sendMessage(Utils.INVALID_USAGE + "'" + name + "' has not joined before or is an invalid player!")
        );
    }
}
