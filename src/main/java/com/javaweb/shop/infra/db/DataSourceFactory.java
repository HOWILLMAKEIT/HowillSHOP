package com.javaweb.shop.infra.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

// 数据源工厂，统一初始化连接池
public final class DataSourceFactory {
    // 连接池单例，避免重复创建导致资源浪费
    private static final HikariDataSource DATA_SOURCE = buildDataSource();

    private DataSourceFactory() {
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }

    private static HikariDataSource buildDataSource() {
        Properties props = new Properties();
        try (InputStream in = DataSourceFactory.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("未找到 db.properties 配置文件。");
            }
            props.load(in);
        } catch (Exception ex) {
            throw new IllegalStateException("读取 db.properties 失败。", ex);
        }

        // .env 优先级高于 db.properties，便于本地与部署覆盖
        overrideFromEnvFile(props);
        overrideFromEnv(props, "DB_DRIVER", "db.driver");
        overrideFromEnv(props, "DB_URL", "db.url");
        overrideFromEnv(props, "DB_USERNAME", "db.username");
        overrideFromEnv(props, "DB_PASSWORD", "db.password");
        overrideFromEnv(props, "DB_POOL_MAX_SIZE", "db.pool.maxSize");
        overrideFromEnv(props, "DB_POOL_MIN_IDLE", "db.pool.minIdle");
        overrideFromEnv(props, "DB_POOL_CONNECTION_TIMEOUT_MS", "db.pool.connectionTimeoutMs");
        overrideFromEnv(props, "DB_POOL_IDLE_TIMEOUT_MS", "db.pool.idleTimeoutMs");
        overrideFromEnv(props, "DB_POOL_MAX_LIFETIME_MS", "db.pool.maxLifetimeMs");

        // 连接池参数来自配置文件，缺省值兜底
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(props.getProperty("db.driver"));
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));

        config.setMaximumPoolSize(getInt(props, "db.pool.maxSize", 20));
        config.setMinimumIdle(getInt(props, "db.pool.minIdle", 5));
        config.setConnectionTimeout(getLong(props, "db.pool.connectionTimeoutMs", 30000));
        config.setIdleTimeout(getLong(props, "db.pool.idleTimeoutMs", 600000));
        config.setMaxLifetime(getLong(props, "db.pool.maxLifetimeMs", 1800000));

        return new HikariDataSource(config);
    }

    private static void overrideFromEnv(Properties props, String envKey, String propKey) {
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            props.setProperty(propKey, value);
        }
    }

    private static void overrideFromEnvFile(Properties props) {
        String envPath = System.getenv("ENV_FILE");
        if (isBlank(envPath)) {
            envPath = System.getProperty("env.file");
        }
        if (isBlank(envPath)) {
            envPath = ".env";
        }

        Path path = Paths.get(envPath);
        if (!Files.exists(path)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                value = stripQuotes(value);
                applyEnvValue(props, key, value);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(".env 文件读取失败。", ex);
        }
    }

    private static void applyEnvValue(Properties props, String key, String value) {
        if (isBlank(value)) {
            return;
        }
        String upper = key.trim().toUpperCase();
        if ("DB_DRIVER".equals(upper)) {
            props.setProperty("db.driver", value);
        } else if ("DB_URL".equals(upper)) {
            props.setProperty("db.url", value);
        } else if ("DB_USERNAME".equals(upper)) {
            props.setProperty("db.username", value);
        } else if ("DB_PASSWORD".equals(upper)) {
            props.setProperty("db.password", value);
        } else if ("DB_POOL_MAX_SIZE".equals(upper)) {
            props.setProperty("db.pool.maxSize", value);
        } else if ("DB_POOL_MIN_IDLE".equals(upper)) {
            props.setProperty("db.pool.minIdle", value);
        } else if ("DB_POOL_CONNECTION_TIMEOUT_MS".equals(upper)) {
            props.setProperty("db.pool.connectionTimeoutMs", value);
        } else if ("DB_POOL_IDLE_TIMEOUT_MS".equals(upper)) {
            props.setProperty("db.pool.idleTimeoutMs", value);
        } else if ("DB_POOL_MAX_LIFETIME_MS".equals(upper)) {
            props.setProperty("db.pool.maxLifetimeMs", value);
        } else if (key.startsWith("db.")) {
            props.setProperty(key, value);
        }
    }

    private static String stripQuotes(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static int getInt(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    private static long getLong(Properties props, String key, long defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(value.trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
