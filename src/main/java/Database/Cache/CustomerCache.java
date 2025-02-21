package Database.Cache;

import Database.Jedis.RedisConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ObjectClass.Item;
import java.lang.reflect.Type;
import java.util.List;
import redis.clients.jedis.Jedis;

public class CustomerCache {
    private static final String CART_KEY_PREFIX = "cart:";
    private static final String SAVED_KEY_PREFIX = "saved:";
    private static final String WALLET_KEY_PREFIX = "wallet:";

    // Time-to-live (TTL) values in seconds.
    private static final int CART_CACHE_TTL = 3600;   // 1 hour for cart items
    private static final int SAVED_CACHE_TTL = 3600;    // 1 hour for saved items
    private static final int WALLET_CACHE_TTL = 3600;   // 1 hour for wallet data

    // Cache cart items for a user.
    public static void cacheCartItems(int userId, List<Item> cartItems) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            Gson gson = new Gson();
            String json = gson.toJson(cartItems);
            String key = CART_KEY_PREFIX + userId;
            jedis.set(key, json);
            jedis.expire(key, CART_CACHE_TTL);
        }
    }

    // Retrieve cached cart items for a user.
    public static List<Item> getCachedCartItems(int userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = CART_KEY_PREFIX + userId;
            String json = jedis.get(key);
            if (json != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Item>>(){}.getType();
                return gson.fromJson(json, listType);
            }
        }
        return null;
    }

    // Clear the cart cache for a user.
    public static void clearCartCache(int userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = CART_KEY_PREFIX + userId;
            jedis.del(key);
        }
    }

    // Cache saved-for-later items for a user.
    public static void cacheSavedForLaterItems(int userId, List<Item> savedItems) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            Gson gson = new Gson();
            String json = gson.toJson(savedItems);
            String key = SAVED_KEY_PREFIX + userId;
            jedis.set(key, json);
            jedis.expire(key, SAVED_CACHE_TTL);
        }
    }

    // Retrieve cached saved-for-later items for a user.
    public static List<Item> getCachedSavedForLaterItems(int userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = SAVED_KEY_PREFIX + userId;
            String json = jedis.get(key);
            if (json != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Item>>(){}.getType();
                return gson.fromJson(json, listType);
            }
        }
        return null;
    }

    // Clear the saved-for-later cache for a user.
    public static void clearSavedForLaterCache(int userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = SAVED_KEY_PREFIX + userId;
            jedis.del(key);
        }
    }

    // Cache wallet discount points for a user.
    public static void cacheWalletPoints(int userId, int discountPoints) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = WALLET_KEY_PREFIX + userId;
            jedis.set(key, String.valueOf(discountPoints));
            jedis.expire(key, WALLET_CACHE_TTL);
        }
    }

    // Retrieve cached wallet discount points for a user.
    public static Integer getCachedWalletPoints(int userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = WALLET_KEY_PREFIX + userId;
            String pointsStr = jedis.get(key);
            if (pointsStr != null) {
                return Integer.parseInt(pointsStr);
            }
        }
        return null;
    }

    // Clear the wallet cache for a user.
    public static void clearWalletCache(int userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = WALLET_KEY_PREFIX + userId;
            jedis.del(key);
        }
    }
}
