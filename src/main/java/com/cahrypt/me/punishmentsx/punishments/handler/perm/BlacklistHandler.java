package com.cahrypt.me.punishmentsx.punishments.handler.perm;

import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import com.cahrypt.me.punishmentsx.punishments.PunishmentHandler;
import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BlacklistHandler extends PunishmentHandler {

    public BlacklistHandler() {
        super("blacklist");
    }

    @Override
    protected boolean validatePunishment(@NotNull CommandSender sender, @NotNull String[] rawArgs, @NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo) {
        return true;
    }

    @Override
    protected void onPunishmentPlace(@NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo) {
        OfflinePlayer target = targetInfo.getOfflinePlayer();

        if (target.isOnline()) {
            target.getPlayer().kickPlayer(ChatColor.RED + "" + ChatColor.UNDERLINE + "You have been blacklisted");
        }
    }
}
