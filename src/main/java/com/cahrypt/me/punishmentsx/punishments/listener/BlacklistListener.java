package com.cahrypt.me.punishmentsx.punishments.listener;

import com.cahrypt.me.punishmentsx.punishments.ActivePunishmentListener;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class BlacklistListener extends ActivePunishmentListener {

    public BlacklistListener() {
        super(PunishmentManager.PunishmentStorage.BLACKLIST_STORAGE);
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        storage.consumeActivePunishmentInfo(event.getName(), info -> event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.RED + "You are blacklisted from this server"));
    }
}
