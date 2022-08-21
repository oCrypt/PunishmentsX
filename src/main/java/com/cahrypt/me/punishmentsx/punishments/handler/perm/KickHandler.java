package com.cahrypt.me.punishmentsx.punishments.handler.perm;

import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import com.cahrypt.me.punishmentsx.punishments.PunishmentHandler;
import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class KickHandler extends PunishmentHandler {

    public KickHandler() {
        super("kick");
    }

    @Override
    protected boolean validatePunishment(@NotNull CommandSender sender, @NotNull String[] rawArgs, @NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo) {
        OfflinePlayer target = targetInfo.getOfflinePlayer();

        if (target.isOnline()) {
            return true;
        }

        sender.sendMessage(ChatColor.RED + Utils.ERROR_PREFIX + "You tried kicking an offline player. I don't know what you thought would happen, but get some IQ");
        return false;
    }

    @Override
    protected void onPunishmentPlace(@NotNull StorablePlayerInfo targetInfo, @NotNull PunishmentInfo punishmentInfo) {
        targetInfo.getOfflinePlayer().getPlayer().kickPlayer(ChatColor.RED + "You have been kicked\n" + "Reason: " + punishmentInfo.getReason());
    }
}
