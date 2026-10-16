package com.hotel.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Simple JDBC connection helper.
 * Reads src/main/resources/db.properties (packaged to WEB-INF/classes/db.properties).
 * For a bigger production app, prefer a JNDI DataSource configured in Tomcat's context.xml
 * with a connection pool (e.g. Tomcat JDBC Pool or HikariCP) instead of opening a new
 * connection per request.
 */
public class DBConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DRIVER;

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            if (in == null) {
                throw new RuntimeException("db.properties not found on classpath (WEB-INF/classes)");
            }
            props.load(in);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
            DRIVER = props.getProperty("db.driver");
            Class.forName(DRIVER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DBConnection: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
