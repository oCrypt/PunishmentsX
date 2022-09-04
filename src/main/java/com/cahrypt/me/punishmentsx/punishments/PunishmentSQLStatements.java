package com.cahrypt.me.punishmentsx.punishments;

public class PunishmentSQLStatements {
    private final String punishmentTableName;
    private final int maxOffenderIDLength;

    public PunishmentSQLStatements(String punishmentTableName, int maxOffenderIDLength) {
        this.punishmentTableName = punishmentTableName;
        this.maxOffenderIDLength = maxOffenderIDLength;
    }

    /**
     * Obtain the appropriate table creation query
     * @return the table creation query
     */
    public String getPunishmentTableQuery() {
        return "CREATE TABLE IF NOT EXISTS " + punishmentTableName + " (" +
                "offenderID VARCHAR("+ maxOffenderIDLength + ") NOT NULL, " +
                "punisherID VARCHAR(36) NOT NULL, " +
                "reason VARCHAR(50) NOT NULL, " +
                "punishDate TIMESTAMP NOT NULL, " +
                "expirationDate TIMESTAMP NULL DEFAULT NULL, " +
                "pardoned BOOL DEFAULT FALSE, " +
                "pardoner VARCHAR(36) DEFAULT NULL, " +
                "pardonReason VARCHAR(50) DEFAULT NULL, " +
                "PRIMARY KEY (offenderID, punisherID, reason, punishDate) " +
                ");";
    }

    /**
     * Obtain the appropriate punishment placement query
     * @return the punishment placement query
     */
    public String getPunishmentPlaceQuery() {
        return "INSERT INTO " + punishmentTableName + " (offenderID, punisherID, reason, punishDate, expirationDate) VALUES (?, ?, ?, ?, ?);";
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
