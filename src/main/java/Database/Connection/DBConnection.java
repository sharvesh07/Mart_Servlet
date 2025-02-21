package Database.Connection;

import com.System.SysLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/MART";
    private static final String USER = "root";
    private static final String PASSWORD = "1234567890";

    public static String getURL() {
        return URL;
    }

    public static String getUser () {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    /**
     * Obtains a connection from the pool and returns a proxy Connection.
     * The proxy intercepts close() calls to release the connection back to the pool.
     */
    public static Connection getConnection() {
        try {
            // Get the actual connection from the pool
            Connection realConnection = ConnectionPool.getConnection();
            SysLogger.logInfo("DBConnection: Successfully obtained a connection from the pool.");
            // Return a proxy that intercepts the close() method
            return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("close".equals(method.getName())) {
                            // Instead of closing, release the connection back to the pool
                            ConnectionPool.releaseConnection(realConnection);
                            SysLogger.logInfo("DBConnection: Connection released back to the pool.");
                            return null;
                        }
                        return method.invoke(realConnection, args);
                    }
                }
            );
        } catch (InterruptedException e) {
            SysLogger.logWarning("DBConnection: Failed to get connection from pool", e);
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints all rows from a table to the console.
     */
    public static void viewRow(Connection conn, String tableName, String constraint) {
        String query = "SELECT * FROM " + tableName +
                (constraint != null && !constraint.trim().isEmpty() ? " WHERE " + constraint : "") + ";";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println("\n" + "-".repeat(columnCount * 10)); // Separator

            // Print row data
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
            SysLogger.logInfo("DBConnection: Successfully viewed rows from table: " + tableName);
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error viewing rows from table: " + tableName, e);
        }
    }

    /**
     * Retrieves a single row from a table that meets the given constraint.
     */
    public static ArrayList<String> getRow(Connection conn, String tableName, String constraint) {
        ArrayList<String> row = new ArrayList<>();
        String query = "SELECT * FROM " + tableName +
                (constraint != null && !constraint.trim().isEmpty() ? " WHERE " + constraint : "") + ";";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
 for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i)); // Fetch all columns as strings
                }
                SysLogger.logInfo("DBConnection: Successfully retrieved a single row from table: " + tableName);
            }
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error fetching a single row from table: " + tableName, e);
        }
        return row;
    }

    /**
     * Retrieves multiple rows from a table that meet the given constraint.
     */
    public static ArrayList<ArrayList<String>> getRows(Connection conn, String tableName, String constraint) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        String query = "SELECT * FROM " + tableName +
                (constraint != null && !constraint.trim().isEmpty() ? " WHERE " + constraint : "") + ";";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Process each row
            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                result.add(row);
            }
            SysLogger.logInfo("DBConnection: Successfully retrieved multiple rows from table: " + tableName);
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error fetching multiple rows from table: " + tableName, e);
            throw new RuntimeException("Error fetching rows from " + tableName, e);
        }
        return result;
    }

    public static ArrayList<ArrayList<String>> getRows(String tableName, String constraint) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        String query = "SELECT * FROM " + tableName +
                (constraint != null && !constraint.trim().isEmpty() ? " WHERE " + constraint : "") + ";";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection(); // Establish connection inside the method
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Process each row
            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                result.add(row);
            }
            SysLogger.logInfo("DBConnection: Successfully retrieved multiple rows from table: " + tableName);
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error fetching multiple rows from table: " + tableName, e);
            throw new RuntimeException("Error fetching rows from " + tableName, e);
        } finally {
            // Close resources in reverse order
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                SysLogger.logWarning("DBConnection: Error closing resources", e);
            }
        }
        return result;
    }

    /**
     * Adds a new row to the specified table using provided column data.
     * Each entry in columnData should have:
     *   key   : column name
     *   value : a Map.Entry where key is the column type (e.g., INTEGER, VARCHAR) and value is the column value.
     */
    public static boolean addRow(Connection conn, String tableName, List<Map.Entry<String, Map.Entry<String, String>>> columnData) {
        if (columnData == null || columnData.isEmpty()) {
            SysLogger.logWarning("DBConnection: addRow: No column data provided for insert into table: " + tableName);
            return false;
        }

        StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");

        for (Map.Entry<String, Map.Entry<String, String>> entry : columnData) {
            query.append(entry.getKey()).append(", ");
            placeholders.append("?, ");
        }
        // Remove trailing comma and space, then complete the query
        query.setLength(query.length() - 2);
        placeholders.setLength(placeholders.length() - 2);
        query.append(")").append(placeholders).append(");");

        try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            int index = 1;
            for (Map.Entry<String, Map.Entry<String, String>> entry : columnData) {
                String columnType = entry.getValue().getKey();
                String columnValue = entry.getValue().getValue();

                switch (columnType.toUpperCase()) {
                    case "INTEGER":
                        pstmt.setInt(index, Integer.parseInt(columnValue));
                        break;
                    case "DOUBLE":
                    case "FLOAT":
                        pstmt.setDouble(index, Double.parseDouble(columnValue));
                        break;
                    case "BOOLEAN":
                        pstmt.setBoolean(index, Boolean.parseBoolean(columnValue));
                        break;
                    case "DATE":
                        pstmt.setDate(index, Date.valueOf(columnValue));
                        break;
                    case "TIMESTAMP":
                        pstmt.setTimestamp(index, Timestamp.valueOf(columnValue));
                        break;
                    default:
                        pstmt.setString(index, columnValue);
                        break;
                }
                index++;
            }
            int rowsInserted = pstmt.executeUpdate();
            SysLogger.logInfo("DBConnection: Successfully added a row to table: " + tableName);
            return rowsInserted > 0;
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error adding row to table: " + tableName, e);
            return false;
        }
    }

    /**
     * Deletes rows from the specified table based on the given condition.
     * Uses transaction management to ensure data integrity.
     */
    public static boolean deleteRow(Connection conn, String tableName, String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            SysLogger.logWarning("DBConnection: deleteRow: DELETE operation requires a WHERE condition for table: " + tableName);
            return false;
        }
        String query = "DELETE FROM " + tableName + " WHERE " + condition + ";";
        try {
            conn.setAutoCommit(false); // Start transaction
            try (Statement stmt = conn.createStatement()) {
                int affectedRows = stmt.executeUpdate(query);
                conn.commit(); // Commit transaction
                SysLogger.logInfo("DBConnection: Successfully deleted rows from table: " + tableName);
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    SysLogger.logWarning("DBConnection: Error during rollback after failed delete operation", rollbackEx);
                }
            }
            SysLogger.logWarning("DBConnection: Error deleting row from table: " + tableName, e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException closeEx) {
                    SysLogger.logWarning("DBConnection: Error resetting auto-commit after delete operation", closeEx);
                }
            }
        }
    }

    /**
     * Updates rows in the specified table based on the given condition.
     */
    public static boolean updateRow(Connection conn, String tableName, String condition, List<Map.Entry<String, Map.Entry<String, String>>> columnData) {
        if (columnData == null || columnData.isEmpty()) {
            SysLogger.logWarning("DBConnection: updateRow: No column data provided for update in table: " + tableName);
            return false;
        }

        StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        for (Map.Entry<String, Map.Entry<String, String>> entry : columnData) {
            query.append(entry.getKey()).append(" = ?, ");
        }
        // Remove trailing comma and space
        query.setLength(query.length() - 2);
        if (condition != null && !condition.trim().isEmpty()) {
            query.append(" WHERE ").append(condition);
        }
        query.append(";");

        try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            int index = 1;
            for (Map.Entry<String, Map.Entry<String, String>> entry : columnData) {
                String columnType = entry.getValue().getKey();
                String columnValue = entry.getValue().getValue();

                switch (columnType.toUpperCase()) {
                    case "INTEGER":
                        pstmt.setInt(index, Integer.parseInt(columnValue));
                        break;
                    case "DOUBLE":
                    case "FLOAT":
                        pstmt.setDouble(index, Double.parseDouble(columnValue));
                        break;
                    case "BOOLEAN":
                        pstmt.setBoolean(index, Boolean.parseBoolean(columnValue));
                        break;
                    case "DATE":
                        pstmt.setDate(index, Date.valueOf(columnValue));
                        break;
                    case "TIMESTAMP":
                        pstmt.setTimestamp(index, Timestamp.valueOf(columnValue));
                        break;
                    default:
                        pstmt.setString(index, columnValue);
                        break;
                }
                index++;
            }
            int rowsUpdated = pstmt.executeUpdate();
            SysLogger.logInfo("DBConnection: Successfully updated rows in table: " + tableName);
            return rowsUpdated > 0;
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error updating row in table: " + tableName, e);
            return false;
        }
    }

    /**
     * Checks if a given ID exists in a specified table (by column name).
     */
    public static boolean checkIdExists(Connection conn, String tableName, String columnName, String id) {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    SysLogger.logInfo("DBConnection: Checked existence of ID in table: " + tableName + ", exists: " + exists);
                    return exists;
                }
            }
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error checking ID existence in table: " + tableName, e);
        }
        return false;
    }

    /**
     * Checks if a given constraint yields any rows in a table.
     */
    public static boolean checkIdExists(Connection conn, String tableName, String constraint) {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + constraint;
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    SysLogger.logInfo("DBConnection: Checked existence with constraint in table: " + tableName + ", exists: " + exists);
                    return exists;
                }
            }
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error checking ID existence with constraint in table: " + tableName, e);
        }
        return false;
    }

    /**
     * Returns the number of rows in the specified table.
     */
    public static int rowCount(Connection conn, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName + ";";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                SysLogger.logInfo("DBConnection: Counted rows in table: " + tableName + ", count: " + count);
                return count;
            }
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error counting rows in table: " + tableName, e);
        }
        return 0;
    }

    /**
     * Retrieves a single row from the specified table, filtering by column and value.
     */
    public static ArrayList<String> getRow(Connection conn, String tableName, String column, String value) {
        ArrayList<String> row = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE " + column + " = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i)); // Fetch all columns as strings
                }
                SysLogger.logInfo("DBConnection: Successfully retrieved row from table: " + tableName + " with column: " + column + " and value: " + value);
            }
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error fetching row from table: " + tableName + " with column: " + column + " and value: " + value, e);
        }
        return row;
    }

    /**
     * Gets the next available ID from the specified table.
     */
    public static int getNextId(Connection conn, String tableName) {
        String query = "SELECT MAX(id) FROM " + tableName;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int maxId = rs.getInt(1);
                int nextId = (maxId == 0) ? 1 : maxId + 1;
                SysLogger.logInfo("DBConnection: Next available ID from table: " + tableName + ", next ID: " + nextId);
                return nextId;
            }
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error getting next ID from table: " + tableName, e);
        }
        return 1; // Default to 1 if an error occurs
    }

    /**
     * Gets the next available transaction ID from the specified table.
    */
    public static int getNextTransactionId(Connection conn, String tableName) {
        String query = "SELECT COALESCE(MAX(transaction_id), 0) FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int nextTransactionId = rs.getInt(1) + 1;
                SysLogger.logInfo("DBConnection: Next available transaction ID from table: " + tableName + ", next transaction ID: " + nextTransactionId);
                return nextTransactionId;
            }
        } catch (SQLException e) {
            SysLogger.logWarning("DBConnection: Error getting next transaction ID from table: " + tableName, e);
        }
        return 1; // Default to 1 if an error occurs
    }
}