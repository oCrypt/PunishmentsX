package com.cahrypt.me.punishmentsx.player.listener;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import dev.fumaz.commons.bukkit.interfaces.FListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerLeaveListener implements FListener {
    private final PlayerManager playerManager;

    public PlayerLeaveListener() {
        super();
        PunishmentsX main = JavaPlugin.getPlugin(PunishmentsX.class);
        this.playerManager = main.getPlayerManager();
        register(main);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerManager.removePlayerCache(event.getPlayer().getName());
    }
}
