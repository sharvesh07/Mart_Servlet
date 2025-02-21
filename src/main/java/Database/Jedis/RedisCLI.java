package Database.Jedis;

import java.util.Scanner;
import java.util.Set;

public class RedisCLI {
    public static void main(String[] args) {
        // Redis Connection Details
        String localHost = "localhost";
        int localPort = 6379;

        String cloudHost = "redis-11353.c90.us-east-1-3.ec2.redns.redis-cloud.com";
        int cloudPort = 11353;
        String cloudPassword = "1234567890";

        // Redis Service Instances for Local and Cloud
        RedisService localRedis = new RedisService();
        RedisService cloudRedis = new RedisService();

        // Initialize Connections
        System.out.println("🔹 Connecting to Local Redis...");
        RedisConnection.initConnection(localHost, localPort, null);

        System.out.println("\n🔹 Connecting to Cloud Redis...");
        RedisConnection.initConnection(cloudHost, cloudPort, cloudPassword);

        // Run the interactive terminal
        startTerminal(localRedis, cloudRedis);

        // Close Redis connection pool
        RedisConnection.closePool();
    }

    private static void startTerminal(RedisService localRedis, RedisService cloudRedis) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n🚀 Redis CLI started!");
        System.out.println("Use `l <command>` for Local Redis, `c <command>` for Cloud Redis.");
        System.out.println("Type `exit` to quit.");

        while (true) {
            System.out.print("\nRedis> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting Redis CLI...");
                break;
            }
            executeCommand(input, localRedis, cloudRedis);
        }

        scanner.close();
    }

    private static void executeCommand(String input, RedisService localRedis, RedisService cloudRedis) {
        if (input.isEmpty()) return;

        // Extract Redis instance type (l/c) and command
        String[] parts = input.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("❌ Invalid command! Use `l <command>` or `c <command>`.");
            return;
        }

        char instanceType = parts[0].charAt(0);
        String command = parts[1];
        RedisService redisService = (instanceType == 'l') ? localRedis : (instanceType == 'c') ? cloudRedis : null;

        if (redisService == null) {
            System.out.println("❌ Invalid instance type! Use `l` for local or `c` for cloud.");
            return;
        }

        // Process Redis command
        RedisService.handleRedisCommand(command);
    }
}
