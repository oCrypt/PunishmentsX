package com.cahrypt.me.punishmentsx.punishments.handler;

import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.command.Arguments;
import dev.fumaz.commons.bukkit.command.annotation.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class TimedPunishmentHandler extends PunishmentHandler {
    private static final int REASON_INDEX = 2;


    /**
     * Registers a new punishment that will be handled via this instance
     *
     * @param punishmentCommand the command used to run the punishment
     */
    public TimedPunishmentHandler(@NotNull String punishmentCommand) {
        super(punishmentCommand);
    }

    @Override
    @SubCommand(cmdArgStructure = { Arguments.CmdArgumentTypes.OFFLINE_PLAYER, Arguments.CmdArgumentTypes.COMPLEX_TIME, Arguments.CmdArgumentTypes.STRING }, permission = Utils.PUNISHMENT_ADMIN_PERMISSION, exactMatch = false)
    public void onPunishCommand(@NotNull CommandSender sender, @NotNull List<Object> usefulArgs, @NotNull String[] rawArgs) {
        OfflinePlayer target = (OfflinePlayer) usefulArgs.get(0);
        String targetName = target.getName();
        long expiry = (long) usefulArgs.get(1);

        storage.getStorableTargetType().useSpecificPlayerInfoOrElseAsync(
                targetName,
                storableTarget -> {
                    String reason = Utils.concatArray(rawArgs, " ", REASON_INDEX);
                    PunishmentInfo punishmentInfo = new PunishmentInfo(storableTarget, storage.getStorableSender(sender), reason, expiry);
                    tryPunishmentPlace(sender, target, punishmentInfo);
                },
                () -> sender.sendMessage(Utils.INVALID_USAGE + "'" + targetName + "' has not joined before or is an invalid player!")
        );
    }
}
