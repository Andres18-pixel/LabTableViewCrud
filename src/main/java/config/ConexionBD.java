package config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Class.forName("org.postgresql.Driver");

            try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    System.err.println("ERROR: No se encontró db.properties");
                } else {
                    Properties prop = new Properties();
                    prop.load(input);
                    url = prop.getProperty("db.url");
                    user = prop.getProperty("db.user");
                    password = prop.getProperty("db.password");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (url == null) {
            throw new SQLException("La URL es null. Revisa db.properties.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}