E-COMMERCE PLATFORM WITH REDIS CACHING AND CLI INTERFACE
=========================================================

Overview
--------
This project is an e-commerce platform built with Java Servlets, JSPs, and a MySQL database. To improve performance, Redis is integrated as a caching layer using the Jedis library. The application supports various modules including user management, inventory management, customer management, and employee management. In addition, a simple Redis CLI tool is provided for testing and interacting with Redis, along with caching classes that manage customer-related data.

Key Components:
---------------
1. **Java Servlets & JSPs**:
   - Servlets such as UserServlet, InventoryServlet, CustomerServlet, and EmployeeServlet handle HTTP requests,
     session management, and interactions with the database.
   - JSP pages provide the front-end interface for login, registration, inventory, customer, and employee management.

2. **MySQL Database**:
   - The database stores data for Users, Inventory, Cart, Orders (Order_Header and Order_Details), and Wallet.
   - Connection pooling is handled via a dedicated DBConnection class.

3. **Redis Caching (via Jedis)**:
   - Redis is used as an in-memory cache to reduce database load and improve response times.
   - Caching patterns include:
     • Cache-Aside (Lazy Loading): Data is first looked up in Redis. If it’s not available, the system loads data from the database and then caches it.
     • Write-Through / Cache Invalidation: When data is modified (e.g., inventory items added or updated), the cache is updated or cleared to ensure consistency.

4. **Redis CLI Tool (RedisCLI.java)**:
   - A simple command-line interface that allows interactive execution of Redis commands against either a local or cloud Redis instance.
   - The tool supports selecting between local (`l`) and cloud (`c`) instances and processes commands via a helper function in the RedisService class.
   - It initializes connections for both local and cloud Redis servers, then starts an interactive terminal.

5. **CustomerCache Class (CustomerCache.java)**:
   - This class is responsible for caching customer-related data.
   - It caches:
     • Cart items (using key pattern “cart:{userId}”)
     • Saved-for-later items (using key pattern “saved:{userId}”)
     • Wallet discount points (using key pattern “wallet:{userId}”)
   - Each caching method serializes objects to JSON using Gson and sets a TTL (time-to-live) on the cached keys.
   - It also provides methods to retrieve cached data and clear specific cache entries.

Project Structure
-----------------
The project is organized into the following key directories and packages:

/src
   ├── com
   │     ├── ObjectClass
   │     │       ├── User.java
   │     │       ├── Item.java
   │     │       └── Orders.java
   │     ├── Servlets
   │     │       ├── UserServlet.java
   │     │       ├── InventoryServlet.java
   │     │       ├── CustomerServlet.java
   │     │       └── EmployeeServlet.java
   │     └── System
   │             ├── JSONFormatter
   │             ├── TaskLogger.java
   │             └── SysLogger.java
   │
   └── Database
         ├── Connection
         |       ├── ConnectionPool
         │       ├── DBConnection.java
         │       └── AppContextListener.java
         ├── DBOperations
         │       ├── UserDBActions.java
         │       ├── InventoryDBActions.java
         │       └── CustomerDBAction.java
         ├── Jedis
         │       ├── RedisConnection.java
         │       ├── RedisService.java
         │       └── RedisCli       
         └── Cache
                 ├── User
                 ├── CustomerCache.java
                 ├── InventoryCache.java
                 └── EmployeeCache.java

Redis CLI Tool
--------------
- **RedisCLI.java**:
  • Contains a main() method that demonstrates how to connect to both a local and a cloud Redis instance.
  • It initializes the Redis connection pool via RedisConnection.initConnection().
  • Two RedisService instances (for local and cloud) are created.
  • An interactive terminal is started where commands prefixed with "l" or "c" indicate which Redis instance to use.
  • The executeCommand() method parses the input and invokes the appropriate command in RedisService.

Caching Implementation (CustomerCache)
----------------------------------------
- **CustomerCache.java**:
  • Provides static methods to cache and retrieve:
    - Cart items for a user: Stored under keys like "cart:3" for user ID 3.
    - Saved-for-later items: Stored under keys like "saved:3".
    - Wallet discount points: Stored under keys like "wallet:3".
  • Uses Gson to serialize Java objects to JSON and vice versa.
  • Sets an expiration time (TTL) for each cached item to avoid stale data.
  • Methods include:
    - cacheCartItems(), getCachedCartItems(), clearCartCache()
    - cacheSavedForLaterItems(), getCachedSavedForLaterItems(), clearSavedForLaterCache()
    - cacheWalletPoints(), getCachedWalletPoints(), clearWalletCache()

Installation and Setup
----------------------
1. **Prerequisites**:
   - Java 11 or higher
   - Apache Tomcat (or another servlet container)
   - MySQL Server (e.g., database “MART”)
   - Redis Server (for caching)
   - Maven or Gradle (for dependency management)
   - Jedis Library for Redis connectivity

2. **Setup**:
   - Clone the repository.
   - Configure the database connection parameters in DBConnection.java and AppContextListener.java.
   - Configure Redis connection parameters (host, port, password) in RedisConnection.java.
   - Build the project (e.g., using Maven: `mvn clean package`).
   - Deploy the WAR file to your Tomcat server.

Running the Project
-------------------
1. Start MySQL and Redis servers.
2. Start your servlet container (Tomcat).
3. Access the application via a web browser at http://localhost:8080/your-app-context.

Usage
-----
- **User Management**:
  Users can log in and register through the provided JSP pages (login.jsp and register.jsp). The UserServlet handles login and registration, using caching (UserCache) for fast retrieval of user profiles.

- **Inventory Management**:
  Inventory data is managed through InventoryServlet. The InventoryCache class caches the list of inventory items to minimize database hits.

- **Customer and Employee Management**:
  Administrators can manage customers and employees using CustomerServlet and EmployeeServlet, respectively. EmployeeCache is used to cache employee data.

- **Redis CLI**:
  Run RedisCLI.java as a standalone application to interact with both local and cloud Redis instances. This is useful for testing and debugging your Redis cache.

Future Improvements
-------------------
- Implement secure password storage using hashing algorithms (e.g., bcrypt).
- Improve the UI for better user experience and responsiveness.
- Enhance caching strategies with more granular invalidation and performance monitoring.
- Add unit and integration tests for critical components.

License
-------
This project is licensed under the MIT License. See the LICENSE file for details.
