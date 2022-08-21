package com.cahrypt.me.punishmentsx.punishments;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import dev.fumaz.commons.bukkit.interfaces.FListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ActivePunishmentListener implements FListener {
    private final PunishmentManager punishmentManager;
    protected final PunishmentManager.PunishmentStorage storage;

    /**
     * An active punishment listener that enforces placed punishments from the specified storage
     * @param storage the storage to enforce
     */
    public ActivePunishmentListener(PunishmentManager.PunishmentStorage storage) {
        PunishmentsX main = JavaPlugin.getPlugin(PunishmentsX.class);

        this.storage = storage;
        this.punishmentManager = main.getPunishmentManager();
        punishmentManager.registerStorageListener(storage, this);
        register(main);
    }

    /**
     * Unregister the active punishment listener
     */
    @Override
    public void unregister() {
        punishmentManager.unregisterPunishmentListener(storage);
        HandlerList.unregisterAll(this);
    }
}
