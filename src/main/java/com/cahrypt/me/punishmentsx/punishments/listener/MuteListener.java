package com.cahrypt.me.punishmentsx.punishments.listener;

import com.cahrypt.me.punishmentsx.punishments.ActivePunishmentListener;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import com.cahrypt.me.punishmentsx.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.Timestamp;

public class MuteListener extends ActivePunishmentListener {

    public MuteListener() {
        super(PunishmentManager.PunishmentStorage.MUTE_STORAGE);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        storage.consumeActivePunishmentInfo(player.getName(), info -> {
            event.setCancelled(true);
            Timestamp expiry = info.getExpiry();

            player.sendMessage(
                    ChatColor.RED + Utils.ERROR_PREFIX + "You are muted!" + "\n" +
                            ChatColor.GRAY + "Expiry: " + (expiry == null ? "Permanent" : Utils.formatTimestamp(expiry)) + "\n" +
                            ChatColor.GRAY + "Reason: " + info.getReason()
                    );
                }
        );
    }
}
