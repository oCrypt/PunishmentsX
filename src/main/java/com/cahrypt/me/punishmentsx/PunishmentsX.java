package com.cahrypt.me.punishmentsx;

import com.cahrypt.me.punishmentsx.command.InfoCmd;
import com.cahrypt.me.punishmentsx.command.PardonCmd;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.player.listener.PlayerJoinListener;
import com.cahrypt.me.punishmentsx.player.listener.PlayerLeaveListener;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import com.cahrypt.me.punishmentsx.punishments.listener.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class PunishmentsX extends JavaPlugin {
    private PunishmentManager punishmentManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {

        // PLAYER

        this.playerManager = new PlayerManager();
        new PlayerJoinListener();
        new PlayerLeaveListener();

        // PUNISHMENT

        this.punishmentManager = new PunishmentManager();

        // LISTENER

        new BanListener();
        new BlacklistListener();
        new MuteListener();

        punishmentManager.reportListenerStatus();

        // COMMAND

        new InfoCmd();

        new PardonCmd("unmute", PunishmentManager.PunishmentStorage.MUTE_STORAGE);
        new PardonCmd("unban", PunishmentManager.PunishmentStorage.BAN_STORAGE);
        new PardonCmd("unblacklist", PunishmentManager.PunishmentStorage.BLACKLIST_STORAGE);
    }

    @Override
    public void onDisable() {

    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
