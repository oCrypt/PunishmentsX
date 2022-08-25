package com.cahrypt.me.punishmentsx.punishments.handler.temp;

import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.punishments.handler.TimedPunishmentHandler;
import com.cahrypt.me.punishmentsx.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TempMuteHandler extends TimedPunishmentHandler {

    public TempMuteHandler() {
        super("tempmute");
    }

    @Override
    protected boolean validatePunishment(@NotNull CommandSender sender, @NotNull OfflinePlayer target) {
        return true;
    }

    @Override
    protected void onPunishmentPlace(@NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo) {
        if (target.isOnline()) {
            target.getPlayer().sendMessage(Utils.ERROR_PREFIX + "You have been muted!");
        }
    }
}
