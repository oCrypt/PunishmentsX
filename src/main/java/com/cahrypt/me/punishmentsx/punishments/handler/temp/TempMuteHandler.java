package com.cahrypt.me.punishmentsx.punishments.handler.temp;

import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.punishments.TimedPunishmentHandler;
import com.cahrypt.me.punishmentsx.util.Utils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TempMuteHandler extends TimedPunishmentHandler {

    public TempMuteHandler() {
        super("tempmute");
    }

    @Override
    protected void onPunishmentPlace(@NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo) {
        if (target.isOnline()) {
            target.getPlayer().sendMessage(Utils.ERROR_PREFIX + "You have been muted!");
        }
    }
}
