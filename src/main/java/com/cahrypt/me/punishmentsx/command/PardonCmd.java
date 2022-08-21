package com.cahrypt.me.punishmentsx.command;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.command.CmdArguments;
import dev.fumaz.commons.bukkit.command.VoidCommandExecutor;
import dev.fumaz.commons.bukkit.command.annotation.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PardonCmd extends VoidCommandExecutor {
    private final PlayerManager playerManager;
    private final PluginCommand pluginCommand;
    private final PunishmentManager.PunishmentStorage storage;

    public PardonCmd(String commandName, PunishmentManager.PunishmentStorage storage) {
        super(PunishmentsX.class, commandName);
        this.playerManager = JavaPlugin.getPlugin(PunishmentsX.class).getPlayerManager();
        this.pluginCommand = Bukkit.getPluginCommand(commandName);
        this.storage = storage;
    }

    @Override
    protected void onInvalidCommandUsage(@NotNull CommandSender commandSender, @NotNull String[] cmdArgs) {
        int argsLength = cmdArgs.length;
        commandSender.sendMessage(Utils.INVALID_USAGE + (argsLength < 2 ? "Please use " +  pluginCommand.getUsage() : "'" + cmdArgs[0] + "' is an invalid player or hasn't joined before!"));
    }

    @Override
    protected void onInvalidPermissionUsage(@NotNull CommandSender commandSender) {
        commandSender.sendMessage(Utils.ERROR_PREFIX + "You do not have valid permissions to pardon players!");
    }

    @Override
    protected void onNoArgCommand(@NotNull CommandSender commandSender) {
        onInvalidCommandUsage(commandSender, new String[]{});
    }

    @SubCommand(cmdArgStructure = { CmdArguments.CmdArgumentTypes.OFFLINE_PLAYER, CmdArguments.CmdArgumentTypes.STRING }, permission = Utils.PUNISHMENT_ADMIN_PERMISSION, exactMatch = false)
    public void onInfoCommand(CommandSender sender, List<Object> usefulArgs, String[] cmdArgs) {
        String targetName = ((OfflinePlayer)usefulArgs.get(0)).getName();

        playerManager.usePlayerInfoOrElseASync(targetName, info -> storage.logPardonASync(sender, info, Utils.concatArray(cmdArgs, " ", usefulArgs.size() - 1)), str -> {});
        sender.sendMessage(Utils.SUCCESS_PREFIX + targetName + " has been successfully pardoned");
    }
}
