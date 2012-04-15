package com.jnispace.isll.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import com.jnispace.isll.config.ConfigFieldValidator;
import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.ConnectionToIcescrumException;

public class ConnectionProvider {
    private static ConnectionProvider instance;

    private HashMap<String, Connection> cache = new HashMap<String, Connection>();

    public static ConnectionProvider getInstance() {
        if (instance == null) {
            instance = new ConnectionProvider();
        }
        return instance;
    }

    protected ConnectionProvider() {
    }

    public Connection get(ISLLConfig config) {
        if (!cache.containsKey(config.jdbcConnection)) {
            cache.put(config.jdbcConnection, getConnection(config));
        }

        try {
            if (cache.get(config.jdbcConnection).isClosed()) {
                cache.put(config.jdbcConnection, getConnection(config));
            }
        } catch (SQLException e) {
            throw new ConnectionToIcescrumException("Unable to check if connection is closed");
        }
        return cache.get(config.jdbcConnection);
    }

    private Connection getConnection(ISLLConfig config) {
        try {
            Class.forName(config.jdbcClass);
            return getConnectionFromConfig(config);
        } catch (ClassNotFoundException e) {
            throw new ConnectionToIcescrumException("Driver not found : " + config.jdbcClass);
        } catch (SQLException e) {
            throw new ConnectionToIcescrumException("Invalid database connection string and/or invalid credentials");
        }

    }

    protected Connection getConnectionFromConfig(ISLLConfig config) throws SQLException {
        DriverManager.setLoginTimeout(2);
        if (ConfigFieldValidator.shouldInclude(config.dbUsername)) {
            return DriverManager.getConnection(config.jdbcConnection, config.dbUsername, config.dbPassword);
        } else {
            return DriverManager.getConnection(config.jdbcConnection);
        }
    }
}
