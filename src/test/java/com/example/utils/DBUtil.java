package com.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Plain JDBC helper. One connection per thread, opened/closed by DatabaseHooks for @db scenarios.
 * Connection details (db.url, db.username, db.password) come from config.properties for the active env.
 * All values are passed as PreparedStatement parameters; table/column names go through {@link #identifier}.
 */
public final class DBUtil {

    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,63}");

    private DBUtil() {
    }

    public static void connect() {
        String url = ConfigReader.get("db.url");
        String user = ConfigReader.get("db.username");
        try {
            DriverManager.setLoginTimeout(ConfigReader.getInt("db.timeout"));
            CONNECTION.set(DriverManager.getConnection(url, user, ConfigReader.get("db.password")));
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not connect to " + url + " as '" + user + "': " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        Connection connection = CONNECTION.get();
        if (connection == null) {
            throw new IllegalStateException("No DB connection - is the scenario tagged @db?");
        }
        return connection;
    }

    public static void close() {
        Connection connection = CONNECTION.get();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nothing useful to do on close failure
            } finally {
                CONNECTION.remove();
            }
        }
    }

    /** Runs a SELECT and returns each row as column-label -> value, in column order. */
    public static List<Map<String, Object>> query(String sql, Object... params) {
        try (PreparedStatement ps = prepare(sql, params);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            throw new IllegalStateException("Query failed: " + sql + " -> " + e.getMessage(), e);
        }
    }

    /** Returns the first column of the first row, or null when there are no rows. */
    public static Object queryForValue(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0).values().iterator().next();
    }

    public static long queryForCount(String sql, Object... params) {
        Object value = queryForValue(sql, params);
        return value == null ? 0 : ((Number) value).longValue();
    }

    /** Runs INSERT / UPDATE / DELETE and returns the number of affected rows. */
    public static int update(String sql, Object... params) {
        try (PreparedStatement ps = prepare(sql, params)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Update failed: " + sql + " -> " + e.getMessage(), e);
        }
    }

    /**
     * Validates a table or column name coming from a feature file.
     * Names cannot be bound as parameters, so this guards against SQL injection.
     */
    public static String identifier(String name) {
        if (name == null || !IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid table/column name: " + name);
        }
        return "`" + name + "`";
    }

    private static PreparedStatement prepare(String sql, Object... params) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }
}
