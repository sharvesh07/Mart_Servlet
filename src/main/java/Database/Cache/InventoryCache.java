package Database.Cache;

import Database.Jedis.RedisConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ObjectClass.Item;
import redis.clients.jedis.Jedis;
import java.lang.reflect.Type;
import java.util.List;

public class InventoryCache {
    private static final String INVENTORY_KEY = "inventory:all";
    // TTL for the inventory cache (in seconds); adjust as needed.
    private static final int INVENTORY_CACHE_TTL = 600; // e.g., 10 minutes

    // Cache the full list of inventory items.
    public static void cacheInventory(List<Item> items) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            Gson gson = new Gson();
            String json = gson.toJson(items);
            jedis.set(INVENTORY_KEY, json);
            jedis.expire(INVENTORY_KEY, INVENTORY_CACHE_TTL);
        }
    }

    // Retrieve the cached inventory list.
    public static List<Item> getCachedInventory() {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String json = jedis.get(INVENTORY_KEY);
            if (json != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Item>>(){}.getType();
                return gson.fromJson(json, listType);
            }
        }
        return null;
    }

    // Invalidate the inventory cache.
    public static void clearInventoryCache() {
        try (Jedis jedis = RedisConnection.getConnection()) {
            jedis.del(INVENTORY_KEY);
        }
    }
}
