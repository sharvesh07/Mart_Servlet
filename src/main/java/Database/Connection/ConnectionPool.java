package Database.Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import com.System.SysLogger;

public class ConnectionPool {
    private static String url;
    private static String username;
    private static String password;
    private static int poolSize;
    private static BlockingQueue<Connection> connectionPool;
    private static volatile boolean isInitialized = false;

    private ConnectionPool() {} // Private constructor to prevent instantiation

    public static void initialize(String dbUrl, String dbUsername, String dbPassword, int size) {
        if (isInitialized) {
            SysLogger.logWarning("DB_INIT: Pool already initialized.");
            return;
        }

        synchronized (ConnectionPool.class) {
            if (!isInitialized) {
                url = dbUrl;
                username = dbUsername;
                password = dbPassword;
                poolSize = size;

                if (createConnectionPool()) {
                    isInitialized = true;
                    SysLogger.logInfo("DB_INIT: Connection pool initialized with size: " + poolSize);
                } else {
                    SysLogger.logSevere("DB_INIT: Failed to initialize connection pool.");
                }
            }
        }
    }

    private static boolean createConnectionPool() {
        try {
            connectionPool = new ArrayBlockingQueue<>(poolSize);
            int successCount = 0;

            for (int i = 0; i < poolSize; i++) {
                Connection conn = createConnection();
                if (conn != null) {
                    connectionPool.add(conn);
                    successCount++;
                } else {
                    SysLogger.logWarning("DB_POOL: Skipping null connection.");
                }
            }

            if (successCount == 0) {
                return false;
            }

            SysLogger.logInfo("DB_POOL: Connection pool created successfully with " + successCount + " connections.");
            return true;
        } catch (Exception e) {
            SysLogger.logSevere("DB_POOL: Failure during connection pool creation.", e);
            return false;
        }
    }

    private static Connection createConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            SysLogger.logSevere("DB_POOL: JDBC Driver not found.", e);
        } catch (SQLException e) {
            SysLogger.logSevere("DB_POOL: Failed to create connection.", e);
        }
        return null;
    }

    public static Connection getConnection() throws InterruptedException, SQLException {
        Connection connection = connectionPool.take(); // Blocks if no connection is available

        if (!isValidConnection(connection)) {
            SysLogger.logWarning("DB_POOL: Invalid connection retrieved. Recreating connection.");
            Connection newConnection = createConnection();
            if (newConnection != null) {
                return newConnection;
            } else {
                SysLogger.logSevere("DB_POOL: Unable to create a new connection.");
                throw new SQLException("Unable to create a valid database connection.");
            }
        }
        return connection;
    }

    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connectionPool.put(connection);
                    SysLogger.logInfo("DB_POOL: Connection released to the pool.");
                } else {
                    SysLogger.logWarning("DB_POOL: Attempted to release a closed connection.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                SysLogger.logSevere("DB_POOL: Release connection interrupted.", e);
            } catch (SQLException e) {
                SysLogger.logSevere("DB_POOL: Error checking connection status.", e);
            }
        }
    }

    public static synchronized void closeConnectionPool() {
        if (!isInitialized) {
            SysLogger.logWarning("DB_POOL: Pool is already closed.");
            return;
        }

        while (!connectionPool.isEmpty()) {
            try {
                Connection conn = connectionPool.poll(); // Retrieve and remove connection
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    SysLogger.logInfo("DB_POOL: Connection closed.");
                }
            } catch (SQLException e) {
                SysLogger.logSevere("DB_POOL: Failure closing connection.", e);
            }
        }

        isInitialized = false;
        SysLogger.logInfo("DB_POOL: Connection pool closed.");
    }

    private static boolean isValidConnection(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            SysLogger.logWarning("DB_POOL: Validation error.", e);
            return false;
        }
    }
}
