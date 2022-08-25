package com.cahrypt.me.punishmentsx.punishments.handler;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.command.VoidPunishmentCmd;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import com.cahrypt.me.punishmentsx.storage.DataSource;
import com.cahrypt.me.punishmentsx.util.Utils;
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
    public PunishmentHandler(@NotNull String punishmentCommand) {
        super(PunishmentsX.class, punishmentCommand);
        PunishmentsX main = JavaPlugin.getPlugin(PunishmentsX.class);

        this.punishmentCommand = main.getCommand(punishmentCommand);

        this.database = DataSource.getHikariDatabase();
        this.playerManager = main.getPlayerManager();

        main.getCommand(punishmentCommand).setExecutor(this);
    }

    /**
     * Whether the command should proceed with execution or be terminated
     * @param sender the command sender
     * @param target the target
     * @return whether the command should proceed with execution
     */
    protected abstract boolean validatePunishment(@NotNull CommandSender sender, @NotNull OfflinePlayer target);

    /**
     * Specify what should be done when the punishment is placed
     * @param target the target
     * @param punishmentInfo the SQL-storable punishment information
     */
    protected abstract void onPunishmentPlace(@NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo);

    /**
     * Sets the punishment's storage
     * @param storage the storage
     */
    public void registerStorage(@NotNull PunishmentManager.PunishmentStorage storage) {
        this.storage = storage;
    }

    /**
     * Attempts to write the provided {@link PunishmentInfo} to the database and handles punishment placement
     * @param sender the punishment sender
     * @param target the punishment target
     * @param punishmentInfo the punishment information
     */
    protected void tryPunishmentPlace(@NotNull CommandSender sender, @NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo) {
        if (!validatePunishment(sender, target)) {
            return;
        }

        storage.logExplicitPardon(sender, punishmentInfo.getStorableTarget(), "Overridden Punishment");
        storage.logPlaceAsync(punishmentInfo);
        onPunishmentPlace(target, punishmentInfo);

        String punishmentCommandName = punishmentCommand.getName();
        String targetName = target.getName();

        sender.sendMessage(Utils.SUCCESS_PREFIX + targetName + " has successfully received a " + punishmentCommandName);
        Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(ChatColor.RED + targetName + ChatColor.GRAY + " has received a " + punishmentCommand.getName() + " from " + ChatColor.RED + sender.getName() + ChatColor.GRAY + (player.hasPermission(Utils.PUNISHMENT_ADMIN_PERMISSION) ? " for " + punishmentInfo.getReason() : "!"))
        );
    }

    /**
     * The annotated {@link dev.fumaz.commons.bukkit.command.annotation.SubCommand} method that handles punishment assembly
     * @param sender the punishment sender
     * @param usefulArgs the useful arguments obtained by the {@link dev.fumaz.commons.bukkit.command.annotation.SubCommand}
     * @param rawArgs the raw arguments of the command
     */
    public abstract void onPunishCommand(@NotNull CommandSender sender, @NotNull List<Object> usefulArgs, @NotNull String[] rawArgs);
}
