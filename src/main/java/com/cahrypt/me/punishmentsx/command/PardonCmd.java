package com.cahrypt.me.punishmentsx.command;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.command.CmdArguments;
import dev.fumaz.commons.bukkit.command.annotation.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PardonCmd extends VoidPunishmentCmd {
    private final PunishmentManager.PunishmentStorage storage;

    public PardonCmd(String commandName, PunishmentManager.PunishmentStorage storage) {
        super(PunishmentsX.class, commandName);
        this.storage = storage;
    }

    @SubCommand(cmdArgStructure = { CmdArguments.CmdArgumentTypes.OFFLINE_PLAYER, CmdArguments.CmdArgumentTypes.STRING }, permission = Utils.PUNISHMENT_ADMIN_PERMISSION, exactMatch = false)
    public void onInfoCommand(CommandSender sender, List<Object> usefulArgs, String[] cmdArgs) {
        OfflinePlayer target = (OfflinePlayer) usefulArgs.get(0);

        storage.logPardonAsync(sender, target, Utils.concatArray(cmdArgs, " ", usefulArgs.size() - 1));
        sender.sendMessage(Utils.SUCCESS_PREFIX + target.getName() + " has been successfully pardoned");
    }
}
