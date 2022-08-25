package com.cahrypt.me.punishmentsx.punishments;

public class PunishmentSQLStatements {
    private final String punishmentTableName;

    public PunishmentSQLStatements(String punishmentTableName) {
        this.punishmentTableName = punishmentTableName;
    }

    /**
     * Obtain the appropriate table creation query
     * @return the table creation query
     */
    public String getPunishmentTableQuery() {
        return "CREATE TABLE IF NOT EXISTS " + punishmentTableName + " (" +
                "offenderID VARCHAR NOT NULL, " +
                "punisherID VARCHAR NOT NULL, " +
                "reason VARCHAR(50) NOT NULL, " +
                "punishDate TIMESTAMP NOT NULL, " +
                "expirationDate TIMESTAMP NOT NULL, " +
                "pardoned BOOL NOT NULL, " +
                "pardoner VARCHAR, " +
                "pardonReason VARCHAR(50), " +
                "PRIMARY KEY (offenderID, punisherID, reason, punishDate)" +
                ");";
    }

    /**
     * Obtain the appropriate punishment placement query
     * @return the punishment placement query
     */
    public String getPunishmentPlaceQuery() {
        return "INSERT INTO " + punishmentTableName + " (offenderID, punisherID, reason, punishDate, expirationDate, pardoned, pardoner, pardonReason) VALUES (?, ?, ?, ?, ?, 0, NULL, NULL);";
    }

    /**
     * Obtain the appropriate punishment pardon query
     * @return the punishment pardon query
     */
    public String getPunishmentPardonQuery() {
        return "UPDATE " + punishmentTableName + " SET pardoned = 1, pardoner = ?, pardonReason = ?, punishDate = punishDate, expirationDate = expirationDate WHERE (pardoned = 0 AND offenderID = ? AND TIMESTAMPDIFF(SECOND, expirationDate, ?) < 0)";
    }

    /**
     * Obtain the appropriate query for fetching active punishments
     * @return the active punishment query
     */
    public String getActivePunishmentsQuery() {
        return "SELECT * FROM " + punishmentTableName + " WHERE (offenderID = ? AND pardoned = FALSE AND TIMESTAMPDIFF(SECOND, expirationDate, ?) < 0);";
    }

    /**
     * Obtain the appropriate query for fetching punishments
     * @return the punishment query
     */
    public String getPunishmentsQuery() {
        return "SELECT * FROM " + punishmentTableName + " WHERE (offenderID = ?) ORDER BY punishDate DESC;";
    }
}
