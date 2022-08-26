package com.cahrypt.me.punishmentsx.player.listener;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import dev.fumaz.commons.bukkit.interfaces.FListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements FListener {
    private final PlayerManager playerManager;

    public PlayerJoinListener() {
        super();
        PunishmentsX main = JavaPlugin.getPlugin(PunishmentsX.class);
        this.playerManager = main.getPlayerManager();
        register(main);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        StorablePlayerInfo storablePlayerInfo = StorablePlayerInfo.from(event.getPlayer());
        storablePlayerInfo.log();
        playerManager.cachePlayerInfo(storablePlayerInfo);
    }
}
