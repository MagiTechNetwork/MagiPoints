package com.magitechserver.magipoints.datastore;

import com.magitechserver.magipoints.MagiPointsSpigot;
import com.magitechserver.magipoints.api.MagiPointsAPI;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Frani on 15/11/2017.
 */
public class DataStore implements MagiPointsAPI {

    private Connection connection;
    private Logger logger;

    public DataStore(Logger logger) {
        this.logger = logger;
        initialize();
    }

    public void initialize() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("~~");
        config.setUsername("~~");
        config.setPassword("~~");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(config);
        try {
            this.connection = ds.getConnection();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not connect to the database! Details:");
            e.printStackTrace();
            return;
        }

        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS players(uuid VARCHAR(36) PRIMARY KEY,points INTEGER);");
            statement.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating tables: " + e.getMessage());
            return;
        }
        logger.log(Level.INFO, "Loading player points...");
        logger.log(Level.INFO, "Player points loaded!");
    }

    public void shutdown() {
        try {
            if (!connection.isClosed()) connection.close();
            logger.log(Level.INFO, "Disconnecting from the MagiPoints database");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public int getPoints(String uuid) {
        this.refreshConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT points FROM players WHERE uuid='" + uuid + "';");
            while (resultSet.next()) {
                int points = resultSet.getInt("points");
                statement.close();
                return points;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void addPoints(String uuid, int points) {
        this.refreshConnection();
        try {
            Statement statement = connection.createStatement();
            statement.execute("INSERT INTO players (uuid, points) VALUES(\"" + uuid + "\", " + points + ")" +
                    " ON DUPLICATE KEY UPDATE points=points+" + points + ";");
            statement.close();
            MagiPointsSpigot.update(uuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean takePoints(String uuid, int points) {
        this.refreshConnection();
        try {
            if (getPoints(uuid) >= points) {
                Statement statement = connection.createStatement();
                statement.execute("INSERT INTO players (uuid, points) VALUES(\"" + uuid + "\", " + points + ")" +
                        " ON DUPLICATE KEY UPDATE points=points-" + points + ";");
                statement.close();
                MagiPointsSpigot.update(uuid);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setPoints(String uuid, int points) {
        this.refreshConnection();
        try {
            Statement statement = connection.createStatement();
            statement.execute("INSERT INTO players (uuid, points) VALUES(\"" + uuid + "\", " + points + ")" +
                    " ON DUPLICATE KEY UPDATE points=" + points + ";");
            statement.close();
            MagiPointsSpigot.update(uuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.initialize();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
