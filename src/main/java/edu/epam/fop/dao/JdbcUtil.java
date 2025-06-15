package edu.epam.fop.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Simple utility for obtaining JDBC connections based on the db.properties file
 * that is already present on the class-path.
 */
public final class JdbcUtil {

    private static final String PROPERTIES_FILE = "/db.properties";
    private static final Properties props = new Properties();

    static {
        try {
            // Load properties
            props.load(JdbcUtil.class.getResourceAsStream(PROPERTIES_FILE));
            // Ensure driver is registered
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to load DB properties or driver: " + e);
        }
    }

    private JdbcUtil() { }

    public static Connection getConnection() throws SQLException {
        return ConnectionPool.getConnection();
    }
} 