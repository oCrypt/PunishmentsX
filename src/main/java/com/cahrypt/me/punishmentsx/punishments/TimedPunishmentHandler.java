package com.cahrypt.me.punishmentsx.punishments;

import com.cahrypt.me.punishmentsx.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class TimedPunishmentHandler extends PunishmentHandler {

    public TimedPunishmentHandler(String punishmentCommand) {
        super(punishmentCommand);
    }

    /**
     * Get the intended expiration date from the specified string argument
     * @param argument the string argument
     * @return the expiration date in milliseconds
     */
    private long getExpirationDateMillis(@NotNull String argument) {
        char[] argCharArray = argument.toCharArray();
        int finalPos = argCharArray.length - 1;
        int numericArgument;

        try {
            numericArgument = Integer.parseInt(new StringBuilder(argument).deleteCharAt(finalPos).toString());
        } catch (NumberFormatException exception) {
            return -1;
        }

        if (numericArgument <= 0) {
            return -1;
        }

        long multiplier = switch (argCharArray[finalPos]) {
            case 's' -> Utils.SEC_MILLIS;
            case 'm' -> Utils.MIN_MILLIS;
            case 'h' -> Utils.HOUR_MILLIS;
            case 'd' -> Utils.DAY_MILLIS;
            case 'w' -> Utils.WEEK_MILLIS;
            case 'M' -> Utils.MONTH_MILLIS;
            case 'Y' -> Utils.YEAR_MILLIS;
            default -> -1;
        };

        return (multiplier == -1 ? -1 : multiplier * numericArgument);
    }

    @Override
    protected boolean validatePunishment(@NotNull CommandSender sender, @NotNull String[] args, @NotNull OfflinePlayer target, @NotNull PunishmentInfo punishmentInfo) {
        long expiry = getExpirationDateMillis(args[1]);

        if (expiry == -1) {
            sender.sendMessage(Utils.INVALID_USAGE + "Please specify a valid time!");
            return false;
        }

        punishmentInfo.setExpirationDate(expiry);
        return true;
    }
}
