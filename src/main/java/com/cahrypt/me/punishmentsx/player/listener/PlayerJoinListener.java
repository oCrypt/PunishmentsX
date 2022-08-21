package com.cahrypt.me.punishmentsx.player.listener;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import dev.fumaz.commons.bukkit.interfaces.FListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements FListener {
    private final PunishmentsX main;

    public PlayerJoinListener() {
        super();
        this.main = JavaPlugin.getPlugin(PunishmentsX.class);
        register(main);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        main.getPlayerManager().logPlayer(event.getPlayer());
    }
}
