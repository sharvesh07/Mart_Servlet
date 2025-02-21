package Database.DBOperations;

import com.ObjectClass.Item;
import com.ObjectClass.Orders;
import Database.Connection.DBConnection;
import com.System.SysLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomerDBAction {

    // View the cart for a given user.
    public static void viewCart(Connection conn, int userId) {
        String tableName = "Cart";
        String condition = "user_id = " + userId;
        DBConnection.viewRow(conn, tableName, condition);
        SysLogger.logInfo("CustomerDBAction: View Cart - User ID: " + userId + " - Cart viewed successfully.");
    }

    // Update the quantity of an item in the cart.
    public static void updateCartItemQuantity(Connection conn, int userId, int itemId, int newQuantity) {
        String tableName = "Cart";
        String condition = "user_id = " + userId + " AND item_id = " + itemId;
        List<Map.Entry<String, Map.Entry<String, String>>> data = new ArrayList<>();
        data.add(Map.entry("quantity", Map.entry("INTEGER", String.valueOf(newQuantity))));

        boolean updated = DBConnection.updateRow(conn, tableName, condition, data);
        SysLogger.logInfo("CustomerDBAction: Update Cart Item - Item ID: " + itemId + ", New Quantity: " + newQuantity + ", User ID: " + userId);
        System.out.println(updated ? "Cart item updated successfully." : "Cart item update failed. Item might not exist.");
    }

    // Remove an item from the cart.
    public static boolean removeFromCart(Connection conn, int userId, int itemId) {
        String condition = "user_id = " + userId + " AND item_id = " + itemId;
        boolean deleted = DBConnection.deleteRow(conn, "Cart", condition);
        SysLogger.logInfo("CustomerDBAction: Remove From Cart - Item ID: " + itemId + ", User ID: " + userId);
        System.out.println(deleted ? "Item successfully removed from cart." : "Failed to remove item. Item might not exist.");
        return deleted;
    }

    // Get the quantity of a specific cart item.
    public static int getCartItemQuantity(Connection conn, int userId, int itemId) {
        String constraint = "user_id = " + userId + " AND item_id = " + itemId;
        String tableName = "Cart";
        ArrayList<String> userCartItem = DBConnection.getRow(conn, tableName, constraint);
        // Assuming the quantity is at index 2 (adjust as per your table structure)
        return userCartItem != null && userCartItem.size() > 2 ? Integer.parseInt(userCartItem.get(2)) : 0;
    }

    // Create a new order transaction.
    public static void orderTransaction(Connection conn, int transactionId, int userId, double totalPrice) {
        String query = "INSERT INTO Order_Header (transaction_id, user_id, total_price, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, transactionId);
            stmt.setInt(2, userId);
            stmt.setDouble(3, totalPrice);
            stmt.executeUpdate();
            SysLogger.logInfo("CustomerDBAction: Order Transaction - Transaction ID: " + transactionId + ", User ID: " + userId);
            System.out.println("Order transaction created with Transaction ID " + transactionId);
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to create order transaction - Transaction ID: "
                    + transactionId + ", User ID: " + userId + " Exception: " + e.getMessage());
        }
    }

    // Transfer cart items into order details.
    public static void transferCartItemsToOrderDetails(Connection conn, int transactionId, int userId) {
        String query = "INSERT INTO Order_Details (transaction_id, item_id, quantity, price_per_unit, total_price) " +
                "SELECT ?, c.item_id, c.quantity, i.price, (c.quantity * i.price) " +
                "FROM Cart c JOIN Inventory i ON c.item_id = i.id WHERE c.user_id = ? AND c.in_cart = 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, transactionId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            SysLogger.logInfo("CustomerDBAction: Transfer Cart Items - Transaction ID: " + transactionId + ", User ID: " + userId);
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to transfer cart items to order details - Transaction ID: "
                    + transactionId + ", User ID: " + userId + " Exception: " + e.getMessage());
        }
    }

    // Retrieve the items in the cart for a user.
    public static ArrayList<Item> getCartItems(Connection conn, int userId) {
        ArrayList<Item> cartItems = new ArrayList<>();
        String query = "SELECT item_id, quantity FROM Cart WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                int quantity = rs.getInt("quantity");
                Optional<Item> opItem = InventoryDBActions.getItem(conn, itemId);
                if (opItem.isPresent()) {
                    Item item = opItem.get();
                    item.setQuantity(quantity);
                    cartItems.add(item);
                }
            }
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to retrieve cart items - User ID: " + userId + " Exception: " + e.getMessage());
        }
        return cartItems;
    }

    // Retrieve current (active) cart items.
    public static ArrayList<Item> getCurrentCartItems(Connection conn, int userId) {
        ArrayList<Item> cartItems = new ArrayList<>();
        String query = "SELECT item_id, quantity FROM Cart WHERE user_id = ? AND in_cart = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                int quantity = rs.getInt("quantity");
                Optional<Item> opItem = InventoryDBActions.getItem(conn, itemId);
                if (opItem.isPresent()) {
                    Item item = opItem.get();
                    item.setQuantity(quantity);
                    cartItems.add(item);
                }
            }
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to retrieve current cart items - User ID: " + userId + " Exception: " + e.getMessage());
        }
        return cartItems;
    }

    // Compute the total price of items in the cart.
    public static double computeCartTotalPrice(Connection conn, int userId) {
        String query = "SELECT SUM(c.quantity * i.price) AS final_total " +
                "FROM Cart c JOIN Inventory i ON c.item_id = i.id " +
                "WHERE c.user_id = ? AND c.in_cart = 1";
        double finalPrice = 0;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                finalPrice = rs.getDouble("final_total");
            }
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to compute cart total price - User ID: " + userId + " Exception: " + e.getMessage());
        }
        return finalPrice;
    }

    // Add an item to the cart (or update quantity if it already exists).
    public static void addToCart(Connection conn, int userId, int itemId, int quantity) {
        String query = "INSERT INTO Cart (user_id, item_id, quantity, in_cart) " +
                "VALUES (?, ?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, quantity);
            int rowsAffected = pstmt.executeUpdate();
            SysLogger.logInfo("CustomerDBAction: Add To Cart - Item ID: " + itemId + ", Quantity: " + quantity + ", User ID: " + userId);
            System.out.println(rowsAffected > 0 ? "Item added to cart successfully!" : "Failed to add item to cart.");
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to add item to cart - Item ID: " + itemId + ", User ID: " + userId + " Exception: " + e.getMessage());
        }
    }

    // Check if a specific item is already in the cart.
    public static boolean isItemInCart(Connection conn, int userId, int itemId) {
        String constraint = "user_id = " + userId + " AND item_id = " + itemId;
        return DBConnection.checkIdExists(conn, "Cart", constraint);
    }

    // Clear the active cart (set in_cart flag to 0 for all items).
    public static void clearCart(Connection conn, int userId) {
        DBConnection.deleteRow(conn, "Cart", "user_id = " + userId + " AND in_cart = 1");
        SysLogger.logInfo("CustomerDBAction: Clear Cart - User ID: " + userId + " - All items removed from cart.");
    }

    // Ensure that a wallet record exists for the user; if not, create one.
    public static void ensureWalletExists(Connection conn, int userId) {
        String tableName = "Wallet";
        String columnName = "user_id";
        if (!DBConnection.checkIdExists(conn, tableName, columnName, String.valueOf(userId))) {
            boolean success = DBConnection.addRow(
                conn,
                tableName,
                List.of(
                    Map.entry("user_id", Map.entry("INTEGER", String.valueOf(userId))),
                    Map.entry("discount_points", Map.entry("INTEGER", "0"))
                )
            );
            if (success) {
                SysLogger.logInfo("CustomerDBAction: Create Wallet - User ID: " + userId + " - New wallet record created.");
                System.out.println("Created a new wallet record for user " + userId);
            } else {
                SysLogger.logWarning("CustomerDBAction: Failed to create wallet - User ID: " + userId);
                System.out.println("Failed to create wallet record for user " + userId);
            }
        } else {
            SysLogger.logWarning("CustomerDBAction: Wallet Exists - User ID: " + userId + " - Wallet record already exists.");
            System.out.println("Wallet record already exists for user " + userId);
        }
    }

    // Get discount points from the user's wallet.
    public static int getDiscountPoints(Connection conn, int userId) {
        String constraint = "user_id = " + userId;
        String tableName = "Wallet";
        String columnName = "discount_points";
        ensureWalletExists(conn, userId);
        ArrayList<String> userWallet = DBConnection.getRow(conn, tableName, constraint);
        return userWallet != null && userWallet.size() > 1 ? Integer.parseInt(userWallet.get(1)) : 0;
    }

    // Update discount points for the user's wallet.
    public static boolean updateDiscountPoints(Connection conn, int userId, int newPoints) {
        String tableName = "Wallet";
        List<Map.Entry<String, Map.Entry<String, String>>> rows = new ArrayList<>();
        rows.add(Map.entry("discount_points", Map.entry("INTEGER", String.valueOf(newPoints))));
        boolean updated = DBConnection.updateRow(conn, tableName, "user_id = " + userId, rows);
        SysLogger.logInfo("CustomerDBAction: Update Discount Points - User ID: " + userId + ", New Points: " + newPoints + " - Update successful: " + updated);
        return updated;
    }

    // Helper method to update cart item status.
    private static void updateCartItemStatus(Connection conn, int userId, int itemId, int status, String action) {
        String query = "UPDATE Cart SET in_cart = ? WHERE user_id = ? AND item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, status);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, itemId);
            pstmt.executeUpdate();
            // Log as info since the update succeeded.
            SysLogger.logInfo("CustomerDBAction: " + action + " - Item ID: " + itemId + ", User ID: " + userId);
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to " + action + " - Item ID: " + itemId + ", User ID: " + userId
                    + " Exception: " + e.getMessage());
        }
    }

    public static void saveItemForLater(Connection conn, int userId, int itemId) {
        updateCartItemStatus(conn, userId, itemId, 0, "Save Item For Later");
    }

    public static void moveToCart(Connection conn, int userId, int itemId) {
        updateCartItemStatus(conn, userId, itemId, 1, "Move To Cart");
    }

    // Load old orders for a user and print basic details.
    public static void loadOldOrders(Connection conn, int userId) {
        String query = "SELECT * FROM Order_Header WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Use info logging for successful retrieval of order details.
                    SysLogger.logInfo("CustomerDBAction: Load Old Orders - Order ID: " + rs.getInt("transaction_id") +
                            ", Total Price: " + rs.getDouble("total_price") +
                            ", Timestamp: " + rs.getTimestamp("timestamp"));
                }
            }
        } catch (SQLException e) {
            SysLogger.logWarning("CustomerDBAction: Failed to load old orders - User ID: " + userId + " Exception: " + e.getMessage());
        }
    }

    // Get items that are saved for later (in_cart = 0).
    public static List<Item> getSavedForLaterItems(Connection conn, int userId) {
        List<Item> savedItems = new ArrayList<>();
        String constraint = "user_id = " + userId + " AND in_cart = 0";
        String tableName = "Cart";
        ArrayList<ArrayList<String>> userCart = DBConnection.getRows(conn, tableName, constraint);
        for (ArrayList<String> row : userCart) {
            int itemId = Integer.parseInt(row.get(1));
            int quantity = Integer.parseInt(row.get(2));
            Optional<Item> opItem = InventoryDBActions.getItem(conn, itemId);
            if (opItem.isPresent()) {
                Item item = opItem.get();
                item.setQuantity(quantity);
                savedItems.add(item);
            }
        }
        return savedItems;
    }

    // Get previous orders for a user.
    public static List<Orders> getPreviousOrders(Connection conn, int userId) {
        List<Orders> ordersHistory = Orders.loadOldOrders(conn, userId);
        return ordersHistory;
    }
}
