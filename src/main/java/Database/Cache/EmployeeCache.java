package Database.Cache;

import Database.Jedis.RedisConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ObjectClass.User;
import redis.clients.jedis.Jedis;
import java.lang.reflect.Type;
import java.util.List;

public class EmployeeCache {
    private static final String EMPLOYEES_KEY = "employees:all";
    private static final int EMPLOYEES_CACHE_TTL = 600; // Cache for 10 minutes

    // Cache the full list of employees.
    public static void cacheEmployees(List<User> employees) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            Gson gson = new Gson();
            String json = gson.toJson(employees);
            jedis.set(EMPLOYEES_KEY, json);
            jedis.expire(EMPLOYEES_KEY, EMPLOYEES_CACHE_TTL);
        }
    }

    // Retrieve the cached list of employees.
    public static List<User> getCachedEmployees() {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String json = jedis.get(EMPLOYEES_KEY);
            if (json != null) {
                Type listType = new TypeToken<List<User>>(){}.getType();
                return new Gson().fromJson(json, listType);
            }
        }
        return null;
    }

    // Invalidate the employees cache.
    public static void clearEmployeesCache() {
        try (Jedis jedis = RedisConnection.getConnection()) {
            jedis.del(EMPLOYEES_KEY);
        }
    }
}
