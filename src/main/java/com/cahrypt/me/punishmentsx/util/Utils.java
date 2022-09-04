package com.cahrypt.me.punishmentsx.util;

import org.bukkit.ChatColor;

import java.sql.Timestamp;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class Utils {

    // MESSAGES

    public static final String ERROR_PREFIX = ChatColor.RED + "[!]" + ChatColor.GRAY + " ";
    public static final String SUCCESS_PREFIX = ChatColor.GREEN + "[✔]" + ChatColor.GRAY + " ";
    public static final String INVALID_USAGE = ERROR_PREFIX + ChatColor.RED + "Invalid Punishment Command Usage! " + ChatColor.GRAY;

    // PERMISSION

    public static final String PUNISHMENT_ADMIN_PERMISSION = "punishment.admin";

    // TIMES

    public static final long SEC_MILLIS = 1000L;
    public static final long MIN_MILLIS = 1000L*60;
    public static final long HOUR_MILLIS = 1000L*60*60;
    public static final long DAY_MILLIS = 1000L*60*60*24;
    public static final long WEEK_MILLIS = 1000L*60*60*24*7;
    public static final long MONTH_MILLIS = 1000L*60*60*24*7*4;
    public static final long YEAR_MILLIS = 1000L*60*60*24*7*4*12;

    public static final long TIME_ZONE_OFFSET = TimeZone.getTimeZone("EST").getRawOffset() - HOUR_MILLIS;

    /**
     * Obtains the correct time in milliseconds for my timezone since I'm not testing locally lmao
     * @return the accurate time zone
     */
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis() + TIME_ZONE_OFFSET;
    }

    /**
     * Formats the provided timestamp in a way that makes it easy to read and interpret
     * @return the formatted timestamp as a string
     */
    public static String formatTimestamp(Timestamp timestamp) {
        StringBuilder builder = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        builder
                .append(Month.of(calendar.get(Calendar.MONTH) + 1).getDisplayName(TextStyle.SHORT, Locale.CANADA))
                .append(" ")
                .append(calendar.get(Calendar.DAY_OF_MONTH))
                .append(", ")
                .append(calendar.get(Calendar.YEAR))
                .append(" at ")
                .append(hour < 10 ? "0" + hour : hour)
                .append(":")
                .append(minute < 10 ? "0" + minute : minute)
                .append(":")
                .append(second < 10 ? "0" + second : second);

        return builder.toString();
    }

    // EXTRA

    /**
     * Concat an array of string arguments using a given separation value and starting index
     * @return the concatenated string
     */
    public static String concatArray(String[] args, String separation, int startingIndex) {
        StringBuilder builder = new StringBuilder();

        for (int i = startingIndex; i < args.length; i++) {
            builder.append(args[i]).append(separation);
        }

        return builder.toString();
    }
}
