package Database.Jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisService {
    // Generic method to execute any Redis command
    public static <T> T execute(RedisCommand<T> command) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            return command.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Functional interface for executing Redis commands
    @FunctionalInterface
    public static interface RedisCommand<T> {
        T execute(Jedis jedis);
    }

    // Set key-value pair
    public static void setValue(String key, String value) {
        execute(jedis -> jedis.set(key, value));
    }

    // Get value by key
    public static String getValue(String key) {
        return execute(jedis -> jedis.get(key));
    }

    // Delete key
    public static void deleteKey(String key) {
        execute(jedis -> jedis.del(key));
    }

    // Check if key exists
    public static boolean exists(String key) {
        return execute(jedis -> jedis.exists(key));
    }

    // Set expiration time for a key
    public static void expire(String key, int seconds) {
        execute(jedis -> jedis.expire(key, seconds));
    }

    // Get remaining time-to-live for a key
    public static long ttl(String key) {
        return execute(jedis -> jedis.ttl(key));
    }

    // Increment a key's value
    public static long incr(String key) {
        return execute(jedis -> jedis.incr(key));
    }

    // Decrement a key's value
    public static long decr(String key) {
        return execute(jedis -> jedis.decr(key));
    }

    // Clear database
    public static void flushDB() {
        execute(Jedis::flushDB);
        System.out.println("Current Redis database cleared!");
    }

    // Clear all databases
    public static void flushAll() {
        execute(Jedis::flushAll);
        System.out.println("All Redis databases cleared!");
    }

    // Get all keys matching a pattern
    public static Set<String> getKeysMatching(String pattern) {
        return execute(jedis -> jedis.keys((pattern == null || pattern.isEmpty()) ? "*" : pattern));
    }

    // List operations
    public static void pushToList(String key, String value, boolean left) {
        execute(jedis -> left ? jedis.lpush(key, value) : jedis.rpush(key, value));
    }

    public static String popFromList(String key, boolean left) {
        return execute(jedis -> left ? jedis.lpop(key) : jedis.rpop(key));
    }

    public static List<String> getList(String key, int start, int end) {
        return execute(jedis -> jedis.lrange(key, start, end));
    }

    // Set operations
    public static void addToSet(String key, String... values) {
        execute(jedis -> jedis.sadd(key, values));
    }

    public static Set<String> getSetMembers(String key) {
        return execute(jedis -> jedis.smembers(key));
    }

    public static void removeFromSet(String key, String value) {
        execute(jedis -> jedis.srem(key, value));
    }

    // Hash operations
    public static void setHashField(String key, String field, String value) {
        execute(jedis -> jedis.hset(key, field, value));
    }

    public static String getHashField(String key, String field) {
        return execute(jedis -> jedis.hget(key, field));
    }

    public static Map<String, String> getAllHashFields(String key) {
        return execute(jedis -> jedis.hgetAll(key));
    }

    public static void removeHashField(String key, String field) {
        execute(jedis -> jedis.hdel(key, field));
    }

    // Sorted Set operations
    public static void addToSortedSet(String key, double score, String value) {
        execute(jedis -> jedis.zadd(key, score, value));
    }

    public static Set<String> getSortedSetRange(String key, int start, int end) {
        return (Set<String>) execute(jedis -> jedis.zrange(key, start, end));
    }

    public static void removeFromSortedSet(String key, String value) {
        execute(jedis -> jedis.zrem(key, value));
    }

    // Execute Redis transaction
    public static List<Object> executeTransaction(RedisTransaction transaction) {
        return execute(jedis -> {
            Transaction tx = jedis.multi();
            List<Response<?>> responses = transaction.execute(tx);
            return tx.exec();
        });
    }

    // Functional interface for transactions
    @FunctionalInterface
    public interface RedisTransaction {
        List<Response<?>> execute(Transaction tx);
    }

    public static void handleRedisCommand(String command) {
        String[] parts = command.split(" ", 3);
        String operation = parts[0].toLowerCase();
        String key = (parts.length > 1) ? parts[1] : null;
        String value = (parts.length > 2) ? parts[2] : null;

        switch (operation) {
            case "set":
                if (key != null && value != null) {
                    setValue(key, value);
                    System.out.println("✅ SET " + key + " = " + value);
                } else {
                    System.out.println("❌ Usage: set <key> <value>");
                }
                break;
            case "get":
                if (key != null) {
                    String result = getValue(key);
                    System.out.println(result != null ? "🔹 GET " + key + " = " + result : "⚠️ Key not found");
                } else {
                    System.out.println("❌ Usage: get <key>");
                }
                break;
            case "del":
                if (key != null) {
                    deleteKey(key);
                    System.out.println("🗑️ Deleted key: " + key);
                } else {
                    System.out.println("❌ Usage: del <key>");
                }
                break;
            case "keys":
                String pattern = (key != null) ? key : "*";
                Set<String> keys = getKeysMatching(pattern);
                System.out.println("🔍 Matching Keys: " + keys);
                break;
            case "flushdb":
                flushDB();
                System.out.println("🔥 Database cleared!");
                break;
            case "flushall":
                flushAll();
                System.out.println("🔥 All databases cleared!");
                break;
            case "inc":
                incr(key);
                String result = getValue(key);
                System.out.println("Incremented " + key + " : " + result);
                break;
            default:
                System.out.println("❌ Unknown command: " + operation);
        }
    }
}
