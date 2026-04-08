package com.perishable.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * HikariCP connection pool configuration.
 *
 * WHY HIKARI?
 *   Your original code opened a new Connection on every method call
 *   and frequently forgot to close them — that's a connection leak
 *   that will crash your app under load. HikariCP:
 *   - Maintains a pool of reusable connections (default: 10)
 *   - Validates connections before handing them out
 *   - Times out leaked connections automatically
 *   - Is the fastest Java connection pool (used by Spring Boot by default)
 *
 * Config is loaded from application.properties — no hardcoded credentials.
 */
public class DatabaseConfig {

    private static HikariDataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            dataSource = createPool();
        }
        return dataSource;
    }

    private static HikariDataSource createPool() {
        Properties props = loadProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url", "jdbc:mysql://localhost:3306/perishable_platform"));
        config.setUsername(props.getProperty("db.username", "root"));
        config.setPassword(props.getProperty("db.password", ""));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool sizing
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300_000);       // 5 minutes
        config.setConnectionTimeout(20_000);  // 20 seconds
        config.setMaxLifetime(1_200_000);     // 20 minutes

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setLeakDetectionThreshold(60_000); // Warn if connection held > 1 minute

        // Performance
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Pool name (appears in logs/JMX)
        config.setPoolName("PerishablePlatform-Pool");

        return new HikariDataSource(config);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load application.properties, using defaults");
        }
        return props;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
