package Database.DBOperations;

import Database.Connection.DBConnection;
import com.ObjectClass.User;
import com.System.SysLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserDBActions {

    static String tableName = "Users";

    // Adds a new user to the database.
    public static boolean addUser(Connection conn, String name, String password, String access) {
        int id = DBConnection.getNextId(conn, tableName);

        boolean idExists = DBConnection.checkIdExists(conn, tableName, "id", String.valueOf(id));
        if (idExists) {
            SysLogger.logWarning("UserDBActions: Add User - ID already exists. Please choose a different ID. User Name: " + name);
            return false;
        }

        List<Map.Entry<String, Map.Entry<String, String>>> userDetails = new ArrayList<>();
        userDetails.add(Map.entry("id", Map.entry("INTEGER", String.valueOf(id))));
        userDetails.add(Map.entry("name", Map.entry("VARCHAR", name)));
        userDetails.add(Map.entry("password", Map.entry("VARCHAR", password)));
        userDetails.add(Map.entry("access", Map.entry("VARCHAR", access)));

        User newUser = new User(id, name, password, access);
        newUser.printUserInfo();

        boolean success = DBConnection.addRow(conn, tableName, userDetails);
        SysLogger.logInfo("UserDBActions: Add User - "
                + (success ? "User added successfully!" : "User addition failed.")
                + " User Name: " + name);
        return success;
    }

    // Deletes a user by their ID.
    public static boolean deleteUser(Connection conn, int userId) {
        String constraint = "id = " + userId;
        boolean success = DBConnection.deleteRow(conn, tableName, constraint);
        SysLogger.logInfo("UserDBActions: Delete User - "
                + (success ? "User deleted successfully." : "User deletion failed.")
                + " User ID: " + userId);
        return success;
    }

    // Updates user details.
    public static void updateUser(Connection conn, User user) {
        String constraint = "id = " + user.getId();
        List<Map.Entry<String, Map.Entry<String, String>>> userDetails = new ArrayList<>();
        userDetails.add(Map.entry("name", Map.entry("VARCHAR", user.getName())));
        userDetails.add(Map.entry("password", Map.entry("VARCHAR", user.getPassword())));

        boolean success = DBConnection.updateRow(conn, tableName, constraint, userDetails);
        SysLogger.logInfo("UserDBActions: Update User - "
                + (success ? "User updated successfully." : "User update failed.")
                + " User ID: " + user.getId());
    }

    // Retrieves a user by their ID.
    public static User getUserById(Connection conn, int userId) {
        String constraint = "id = " + userId;
        ArrayList<String> userData = DBConnection.getRow(conn, tableName, constraint);

        if (userData.isEmpty()) {
            SysLogger.logWarning("UserDBActions: Get User By ID - No user found with ID: " + userId);
            return null;
        }

        return new User(
                Integer.parseInt(userData.get(0)),
                userData.get(1),
                userData.get(2),
                userData.get(3)
        );
    }

    // Retrieves all employees.
    public static List<User> getEmployees(Connection conn) {
        String constraint = "access = 'employee'";
        ArrayList<ArrayList<String>> usersList = DBConnection.getRows(conn, tableName, constraint);

        List<User> users = new ArrayList<>();
        for (ArrayList<String> userData : usersList) {
            User user = new User(
                    Integer.parseInt(userData.get(0)),
                    userData.get(1),
                    userData.get(2),
                    userData.get(3)
            );
            users.add(user);
            SysLogger.logInfo("UserDBActions: Get Employees - Retrieved employee: " + user.getName() + ", Employee ID: " + user.getId());
        }
        return users;
    }

    // Retrieves all customers.
    public static List<User> getAllCustomers(Connection conn) {
        String constraint = "access = 'customer'";
        ArrayList<ArrayList<String>> usersList = DBConnection.getRows(conn, tableName, constraint);
        List<User> users = new ArrayList<>();
        for (ArrayList<String> userData : usersList) {
            users.add(new User(
                    Integer.parseInt(userData.get(0)),
                    userData.get(1),
                    userData.get(2),
                    userData.get(3)
            ));
        }
        return users;
    }

    // Retrieves a user by their username.
    public static User getUserByUsername(Connection conn, String username) {
        ArrayList<String> userData = DBConnection.getRow(conn, tableName, "name", username);

        if (userData.isEmpty()) {
            SysLogger.logWarning("UserDBActions: Get User By Username - No user found with username: " + username);
            return null;
        }

        return new User(
                Integer.parseInt(userData.get(0)),
                userData.get(1),
                userData.get(2),
                userData.get(3)
        );
    }
}
