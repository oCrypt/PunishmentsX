package com.cahrypt.me.punishmentsx.punishments.handler.perm;

import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import com.cahrypt.me.punishmentsx.punishments.PunishmentHandler;
import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MuteHandler extends PunishmentHandler {

    public MuteHandler() {
        super("mute");
    }

    @Override
    protected boolean validatePunishment(@NotNull CommandSender sender, @NotNull String[] rawArgs, @NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo) {
        return true;
    }

    @Override
    protected void onPunishmentPlace(@NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo) {
        OfflinePlayer target = targetInfo.getOfflinePlayer();

        if (target.isOnline()) {
            target.getPlayer().sendMessage(Utils.ERROR_PREFIX + "You have been muted!");
        }
    }
}
