package com.cahrypt.me.punishmentsx.command;

import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.command.VoidCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class VoidPunishmentCmd extends VoidCommandExecutor {
    private final PluginCommand pluginCommand;

    public VoidPunishmentCmd(@NotNull Class<? extends JavaPlugin> clazz, @NotNull String commandName) {
        super(clazz, commandName);
        this.pluginCommand = Bukkit.getPluginCommand(commandName);
    }

    @Override
    protected void onInvalidCommandUsage(@NotNull CommandSender commandSender, @NotNull String[] cmdArgs) {
        int argsLength = cmdArgs.length;
        commandSender.sendMessage(Utils.INVALID_USAGE + (argsLength < 2 ? "Please use " +  pluginCommand.getUsage() : "'" + cmdArgs[0] + "' is an invalid player or hasn't joined before!"));
    }

    @Override
    protected void onInvalidPermissionUsage(@NotNull CommandSender commandSender) {
        commandSender.sendMessage(Utils.ERROR_PREFIX + "You do not have valid permissions to do that!");
    }

    @Override
    protected void onNoArgCommand(@NotNull CommandSender commandSender) {
        onInvalidCommandUsage(commandSender, new String[]{});
    }
}
