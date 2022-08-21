package com.cahrypt.me.punishmentsx.storage;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.fumaz.commons.bukkit.storage.sql.HikariDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;
    private static final HikariDatabase hikariDatabase;

    static {
        config.setJdbcUrl("jdbc:mysql://u2_Jhyze4Y86u:.AG.NT35WsAwAb0JUP!47cAZ@db.fumaz.dev:3306/s2_PUNISHMENTS");
        config.setUsername("u2_woVTMmmc4s");
        config.setPassword(".AG.NT35WsAwAb0JUP!47cAZ");
        config.addDataSourceProperty("cachePrepStmts" , "true");
        config.addDataSourceProperty("prepStmtCacheSize" , "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit" , "2048");
        ds = new HikariDataSource( config );
        hikariDatabase = HikariDatabase.of(PunishmentsX.class, ds);
    }

    private DataSource() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /**
     * Obtain the active hikari database
     * @return the active SQL database
     */
    public static HikariDatabase getHikariDatabase() {
        return hikariDatabase;
    }
}