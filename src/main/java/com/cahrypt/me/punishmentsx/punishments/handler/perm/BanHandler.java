package com.cahrypt.me.punishmentsx.punishments.handler.perm;

import com.cahrypt.me.punishmentsx.punishments.handler.PermPunishmentHandler;
import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BanHandler extends PermPunishmentHandler {

    public BanHandler() {
        super("ban");
    }

    @Override
    protected boolean validatePunishment(@NotNull CommandSender sender, @NotNull OfflinePlayer target) {
        return true;
    }

    @Override
    protected void onPunishmentPlace(@NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo) {
        if (target.isOnline()) {
            target.getPlayer().kickPlayer(ChatColor.RED + "" + ChatColor.UNDERLINE + "You have been banned");
        }
    }
}
