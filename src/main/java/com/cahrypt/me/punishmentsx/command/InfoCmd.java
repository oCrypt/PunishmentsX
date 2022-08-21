package com.cahrypt.me.punishmentsx.command;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.punishments.display.PunishmentInfoDisplay;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.command.CmdArguments;
import dev.fumaz.commons.bukkit.command.PlayerCommandExecutor;
import dev.fumaz.commons.bukkit.command.annotation.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfoCmd extends PlayerCommandExecutor {
    private static final String INFO_COMMAND = "info";

    public InfoCmd() {
        super(PunishmentsX.class, INFO_COMMAND);
    }

    @Override
    protected void onInvalidCommandUsage(@NotNull CommandSender commandSender, @NotNull String[] cmdArgs) {
        int argsLength = cmdArgs.length;
        commandSender.sendMessage(Utils.INVALID_USAGE + (argsLength < 1 ? "Please specify a player to check the info of!" : (argsLength == 1 ?  "Invalid player '" + cmdArgs[0] + "specified. Please try again!" : "Please use " + Bukkit.getPluginCommand(INFO_COMMAND).getUsage())));
    }

    @Override
    protected void onInvalidPermissionUsage(@NotNull CommandSender commandSender) {
        commandSender.sendMessage(Utils.ERROR_PREFIX + "You do not have valid permissions to check the punishment information of players");
    }

    @Override
    public void onNoArgCommand(@NotNull CommandSender commandSender) {
        onInvalidCommandUsage(commandSender, new String[] {});
    }

    @SubCommand(cmdArgStructure = { CmdArguments.CmdArgumentTypes.OFFLINE_PLAYER }, permission = Utils.PUNISHMENT_ADMIN_PERMISSION)
    public void onInfoCommand(CommandSender sender, List<Object> usefulArgs, String[] cmdArgs) {
        OfflinePlayer target = (OfflinePlayer) usefulArgs.get(0);

        new PunishmentInfoDisplay(target).displayPunishmentInfo((Player) sender);
        sender.sendMessage(Utils.SUCCESS_PREFIX + "You are now viewing the punishment history of " + target.getName());
    }
}
