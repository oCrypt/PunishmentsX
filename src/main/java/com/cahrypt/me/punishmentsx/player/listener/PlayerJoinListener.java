package com.cahrypt.me.punishmentsx.player.listener;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import dev.fumaz.commons.bukkit.interfaces.FListener;
import dev.fumaz.commons.bukkit.misc.Scheduler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerJoinListener implements FListener {
    private static final int PRE_LOGIN_EVICTION_DELAY = 5;

    private final PunishmentsX main;
    private final PlayerManager playerManager;

    public PlayerJoinListener() {
        super();
        this.main = JavaPlugin.getPlugin(PunishmentsX.class);
        this.playerManager = main.getPlayerManager();
        register(main);
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        StorablePlayerInfo prevInfo = playerManager.getPreLoginCache(name);

        if (prevInfo != null) {
            prevInfo.cancelEvictionTask();
        }

        StorablePlayerInfo storablePlayerInfo = StorablePlayerInfo.from(event,
                Scheduler.of(main).runTaskLater(() -> playerManager.removePreLoginCache(event.getName()), 20L * PRE_LOGIN_EVICTION_DELAY));
        playerManager.cachePreLoginInfo(storablePlayerInfo);
        storablePlayerInfo.log();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        StorablePlayerInfo storablePlayerInfo = playerManager.getPreLoginCache(player.getName());

        if (storablePlayerInfo == null) {
            player.kickPlayer(ChatColor.RED + "Your login information has not been validly processed! Please try again...");
            return;
        }

        storablePlayerInfo.cancelEvictionTask();
        playerManager.cachePlayerInfo(storablePlayerInfo);
    }
}
