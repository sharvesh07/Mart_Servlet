package Database.Cache;

import Database.Jedis.RedisConnection;
import com.google.gson.Gson;
import com.ObjectClass.User;
import redis.clients.jedis.Jedis;

public class UserCache {
    private static final String USER_KEY_PREFIX = "user:";        // For lookup by user ID
    private static final String USERNAME_KEY_PREFIX = "username:";  // For lookup by username
    private static final int USER_CACHE_TTL = 3600; // Cache time-to-live in seconds (1 hour)

    // Cache a user (for both ID and username keys)
    public static void cacheUser(User user) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            String idKey = USER_KEY_PREFIX + user.getId();
            String usernameKey = USERNAME_KEY_PREFIX + user.getName();
            jedis.set(idKey, userJson);
            jedis.expire(idKey, USER_CACHE_TTL);
            jedis.set(usernameKey, userJson);
            jedis.expire(usernameKey, USER_CACHE_TTL);
        }
    }

    // Retrieve a user from the cache by ID
    public static User getUserById(int userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String idKey = USER_KEY_PREFIX + userId;
            String userJson = jedis.get(idKey);
            if (userJson != null) {
                return new Gson().fromJson(userJson, User.class);
            }
        }
        return null;
    }

    // Retrieve a user from the cache by username
    public static User getUserByUsername(String username) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String usernameKey = USERNAME_KEY_PREFIX + username;
            String userJson = jedis.get(usernameKey);
            if (userJson != null) {
                return new Gson().fromJson(userJson, User.class);
            }
        }
        return null;
    }

    // Invalidate a user's cache entries (both by ID and username)
    public static void clearUserCache(int userId, String username) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            jedis.del(USER_KEY_PREFIX + userId);
            jedis.del(USERNAME_KEY_PREFIX + username);
        }
    }
}

