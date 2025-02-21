package Database.Connection;

import Database.Connection.ConnectionPool;
import Database.Jedis.RedisConnection;
import com.System.SysLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.HashMap;
import java.util.Map;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String systemState = getCurrentSystemState(sce);
        SysLogger.logInfo("[AppContext] Initializing application context. System State: " + systemState);

        try {
            // Initialize database connection pool.
            ConnectionPool.initialize("jdbc:mysql://127.0.0.1:3306/MART", "root", "1234567890", 10);
            SysLogger.logInfo("[AppContext] Connection pool initialized and ready.");
        } catch (Exception e) {
            SysLogger.logSevere("[AppContext] Failed to initialize connection pool.", e);
        }

        try {
            // Initialize Redis connection pool.
            // For example, using host "localhost", port 6379, and no password.
            RedisConnection.initConnection("localhost", 6379, "");
            SysLogger.logInfo("[AppContext] Redis connection pool initialized and ready.");
        } catch (Exception e) {
            SysLogger.logSevere("[AppContext] Failed to initialize Redis connection pool.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        String systemState = getCurrentSystemState(sce);
        SysLogger.logInfo("[AppContext] Destroying application context. System State: " + systemState);

        try {
            ConnectionPool.closeConnectionPool();
            SysLogger.logInfo("[AppContext] Connection pool closed. Application context destroyed.");
        } catch (Exception e) {
            SysLogger.logSevere("[AppContext] Failed to close connection pool.", e);
        }

        try {
            // Close Redis connection pool.
            RedisConnection.closePool();
            SysLogger.logInfo("[AppContext] Redis connection pool closed.");
        } catch (Exception e) {
            SysLogger.logSevere("[AppContext] Failed to close Redis connection pool.", e);
        }
    }

    private String getCurrentSystemState(ServletContextEvent sce) {
        Map<String, Object> state = new HashMap<>();
        state.put("timestamp", System.currentTimeMillis());
        state.put("contextPath", sce.getServletContext().getContextPath());
        state.put("serverInfo", sce.getServletContext().getServerInfo());

        return String.format("Timestamp: %d, Context Path: %s, Server Info: %s",
                state.get("timestamp"), state.get("contextPath"), state.get("serverInfo"));
    }
}
