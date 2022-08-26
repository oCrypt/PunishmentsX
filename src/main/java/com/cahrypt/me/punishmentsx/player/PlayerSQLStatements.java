package com.cahrypt.me.punishmentsx.player;

public class PlayerSQLStatements {

    /**
     * The raw query for creating the player information table
     */
    protected static final String CREATE_PLAYER_INFO_TABLE = "CREATE TABLE IF NOT EXISTS PlayerInfo (" +
            "recentName VARCHAR(16) NOT NULL, " +
            "uuid CHAR(36) NOT NULL, " +
            "recentAddress CHAR(16) NOT NULL, " +
            "PRIMARY KEY (uuid)" +
            ");";

    /**
     * The raw query for inserting a new player into the database
     * (Automatically updates upon duplicate entry)
     */
    protected static final String INSERT_NEW_PLAYER  = "INSERT INTO PlayerInfo VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE recentName = ?, uuid = ?, recentAddress = ?;";

    /**
     * The raw query for fetching a player's stored information
     */
    protected static final String FETCH_PLAYER_INFO = "SELECT * FROM PlayerInfo WHERE (recentName = ?);";
}
