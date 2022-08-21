package com.cahrypt.me.punishmentsx.punishments.handler.temp;

import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.punishments.TimedPunishmentHandler;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TempBanHandler extends TimedPunishmentHandler {

    public TempBanHandler() {
        super("tempban");
    }

    @Override
    protected void onPunishmentPlace(@NotNull StorablePlayerInfo playerInfo, @NotNull PunishmentInfo punishmentInfo) {
        OfflinePlayer target = playerInfo.getOfflinePlayer();

        if (target.isOnline()) {
            target.getPlayer().kickPlayer(ChatColor.RED + "" + ChatColor.UNDERLINE + "You have been temporarily banned");
        }
    }
}
