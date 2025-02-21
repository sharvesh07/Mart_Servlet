package Database.Jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {
    private static JedisPool jedisPool;

    // Initialize connection (Local or Remote)
    public static void initConnection(String host, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10); // Max connections

        if (password == null || password.isEmpty()) {
            jedisPool = new JedisPool(poolConfig, host, port);
        } else {
            jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        }
    }

    // Get a Redis connection from the pool
    public static Jedis getConnection() {
        if (jedisPool == null) {
            throw new IllegalStateException("Redis connection is not initialized. Call initConnection() first.");
        }
        return jedisPool.getResource();
    }

    // Close the pool when shutting down the application
    public static void closePool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
