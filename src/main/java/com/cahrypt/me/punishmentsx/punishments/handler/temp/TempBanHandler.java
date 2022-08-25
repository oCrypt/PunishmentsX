package com.cahrypt.me.punishmentsx.punishments.handler.temp;

import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.punishments.handler.TimedPunishmentHandler;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TempBanHandler extends TimedPunishmentHandler {

    public TempBanHandler() {
        super("tempban");
    }

    @Override
    protected boolean validatePunishment(@NotNull CommandSender sender, @NotNull OfflinePlayer target) {
        return true;
    }

    @Override
    protected void onPunishmentPlace(@NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo) {
        if (target.isOnline()) {
            target.getPlayer().kickPlayer(ChatColor.RED + "" + ChatColor.UNDERLINE + "You have been temporarily banned");
        }
    }
}
