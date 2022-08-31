package com.cahrypt.me.punishmentsx.punishments.listener;

import com.cahrypt.me.punishmentsx.punishments.ActivePunishmentListener;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import com.cahrypt.me.punishmentsx.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.Timestamp;

public class BanListener extends ActivePunishmentListener {

    public BanListener() {
        super(PunishmentManager.PunishmentStorage.BAN_STORAGE);
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        storage.consumeActivePunishmentInfo(event.getName(), info -> {
            Timestamp expiry = info.getExpiry();
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    ChatColor.RED + "You are banned from this server" + "\n" +
                            ChatColor.GRAY + "Expiry: " + (expiry == null ? "Permanent" : Utils.formatTimestamp(expiry)) + "\n" +
                            ChatColor.GRAY + "Reason: " + info.getReason());
                }
        );
    }
}
