package com.cahrypt.me.punishmentsx.punishments;

import com.cahrypt.me.punishmentsx.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PunishmentInfo {
    private String storableTarget;
    private String storableSender;
    private Timestamp punishDate;
    private Timestamp expirationDate;
    private String reason;

    private boolean pardoned;
    private String pardonReason;

    /**
     * Create new punishment information given an SQL-storable target, an SQL-storable sender, and a punishment reason
     * Expiration date defaults as permanent
     * @param storableTarget SQL-storable target
     * @param storableSender SQL-storable sender
     * @param reason punishment reason
     */
    public PunishmentInfo(@NotNull String storableTarget, @NotNull String storableSender, @NotNull String reason) {
        this.storableTarget = storableTarget;
        this.storableSender = storableSender;
        this.punishDate = new Timestamp(Utils.getCurrentTimeMillis());
        this.expirationDate = new Timestamp(Utils.getCurrentTimeMillis() + Utils.PERMANENT_PUNISHMENT_TIME);
        this.reason = reason;

        this.pardoned = false;
        this.pardonReason = null;
    }

    /**
     * Obtain punishment information from the provided {@link ResultSet}
     * @param resultSet the {@link ResultSet} to read from
     * @throws SQLException if the {@link ResultSet} is not appropriate to fetch punishment information from
     */
    public PunishmentInfo(@NotNull ResultSet resultSet) throws SQLException {
        this.storableTarget = resultSet.getString("offenderID");
        this.storableSender = resultSet.getString("punisherID");
        this.punishDate = resultSet.getTimestamp("punishDate");
        this.expirationDate = resultSet.getTimestamp("expirationDate");
        this.reason = resultSet.getString("reason");

        this.pardoned = resultSet.getBoolean("pardoned");
        this.pardonReason = resultSet.getString("pardonReason");
    }

    /**
     * Sets the SQL-storable target
     * @param storableTarget SQL-storable target
     */
    public void setStorableTarget(@NotNull String storableTarget) {
        this.storableTarget = storableTarget;
    }

    /**
     * Sets the SQL-storable sender
     * @param storableSender SQL-storable sender
     */
    public void setStorableSender(@NotNull String storableSender) {
        this.storableSender = storableSender;
    }

    /**
     * Sets the punishment date
     * @param punishDate the {@link Timestamp} of the date
     */
    public void setPunishDate(@NotNull Timestamp punishDate) {
        this.punishDate = punishDate;
    }

    /**
     * Sets the punishment date
     * @param punishDate the time in milliseconds from the current time
     */
    public void setPunishDate(long punishDate) {
        this.punishDate = new Timestamp(Utils.getCurrentTimeMillis() + punishDate);
    }

    /**
     * Sets the expiration date
     * @param expirationDate the {@link Timestamp} of the date
     */
    public void setExpirationDate(@NotNull Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Sets the expiration date
     * @param expirationDate the time in milliseconds the punishment should last
     */
    public void setExpirationDate(long expirationDate) {
        this.expirationDate = new Timestamp(Utils.getCurrentTimeMillis() + expirationDate);
    }

    /**
     * Sets the reason of the punishment
     * @param reason punishment reason
     */
    public void setReason(@NotNull String reason) {
        this.reason = reason;
    }

    /**
     * Sets the status of the punishment
     * @param pardoned punished or not punished
     */
    public void setPardoned(boolean pardoned) {
        this.pardoned = pardoned;
    }

    /**
     * Sets the reason for the pardoning if pardoned
     * @param pardonReason pardon reason
     */
    public void setPardonReason(@NotNull String pardonReason) {
        if (pardoned) {
            this.pardonReason = pardonReason;
        }
    }

    /**
     * @return the SQL-storable target
     */
    @NotNull
    public String getStorableTarget() {
        return storableTarget;
    }

    /**
     * @return the SQL-storable sender
     */
    @NotNull
    public String getStorableSender() {
        return storableSender;
    }

    /**
     * @return the {@link Timestamp} of the punishment date
     */
    @NotNull
    public Timestamp getPunishDate() {
        return punishDate;
    }

    /**
     * @return the {@link Timestamp} of the expiration date
     */
    @NotNull
    public Timestamp getExpiry() {
        return expirationDate;
    }

    /**
     * @return the punishment reason
     */
    @NotNull
    public String getReason() {
        return reason;
    }

    /**
     * @return whether the target is pardoned
     */
    public boolean isPardoned() {
        return pardoned;
    }

    /**
     * @return the pardon reason
     */
    @NotNull
    public String getPardonReason() {
        return pardonReason;
    }
}
