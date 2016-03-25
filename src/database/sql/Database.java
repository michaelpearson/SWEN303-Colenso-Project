package database.sql;

import util.ServerConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    public static Connection getConnection() {
        try {
            Class.forName("org.h2.Driver");
            Connection c = DriverManager.getConnection(ServerConfiguration.getConfigurationString("sqlDatabase", "connectionString"), "", "");
            c.setAutoCommit(true);
            return c;
        } catch(Exception e) {
            throw new RuntimeException("Could not get database");
        }
    }
}
