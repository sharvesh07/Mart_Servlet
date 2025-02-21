package Database.DBOperations;

import Database.Connection.DBConnection;
import com.ObjectClass.Item;
import com.System.SysLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryDBActions {

    public static String tableName = "Inventory";

    // Adds a new item to the inventory.
    public static boolean addItem(Connection conn, String name, double price, int quantity, String category) {
        int id = DBConnection.getNextId(conn, tableName);

        boolean idExist = DBConnection.checkIdExists(conn, tableName, "id", String.valueOf(id));
        if (idExist) {
            SysLogger.logWarning("InventoryDBActions: Add Item - ID already exists. Please choose a different ID. Item Name: " + name);
            return false;
        }

        List<Map.Entry<String, Map.Entry<String, String>>> itemDetails = new ArrayList<>();
        itemDetails.add(Map.entry("id", Map.entry("INTEGER", String.valueOf(id))));
        itemDetails.add(Map.entry("name", Map.entry("VARCHAR", name)));
        itemDetails.add(Map.entry("price", Map.entry("DOUBLE", String.valueOf(price))));
        itemDetails.add(Map.entry("quantity", Map.entry("INTEGER", String.valueOf(quantity))));
        itemDetails.add(Map.entry("category", Map.entry("TEXT", category)));

        boolean success = DBConnection.addRow(conn, tableName, itemDetails);
        SysLogger.logInfo("InventoryDBActions: Add Item - "
                + (success ? "Item added successfully!" : "Item addition failed.")
                + " - Item Name: " + name);
        return success;
    }

    // Retrieves all items from the inventory.
    public static List<Item> getAllItems(Connection conn) {
        ArrayList<ArrayList<String>> inventory = DBConnection.getRows(conn, tableName, "");
        List<Item> items = new ArrayList<>();
        for (ArrayList<String> item : inventory) {
            items.add(new Item(
                Integer.parseInt(item.get(0)),
                item.get(1),
                Double.parseDouble(item.get(2)),
                Integer.parseInt(item.get(3)),
                item.get(4)
            ));
        }
        return items;
    }

    // Retrieves a single item from the inventory based on its ID.
    public static Optional<Item> getItem(Connection conn, int id) {
        String constraint = "id = " + id;
        ArrayList<String> item = DBConnection.getRow(conn, tableName, constraint);
        if (item.isEmpty()) {
            SysLogger.logWarning("InventoryDBActions: Get Item - No item found with ID: " + id);
            return Optional.empty();
        }
        return Optional.of(new Item(
            Integer.parseInt(item.get(0)),
            item.get(1),
            Double.parseDouble(item.get(2)),
            Integer.parseInt(item.get(3)),
            item.get(4)
        ));
    }

    // Updates details of an existing item.
    public static boolean updateItemDetails(Connection conn, int id, String newName, Double newPrice, Integer newQuantity, String newCategory) {
        String constraint = "id = " + id;
        List<Map.Entry<String, Map.Entry<String, String>>> itemDetails = new ArrayList<>();
        if (newName != null) itemDetails.add(Map.entry("name", Map.entry("VARCHAR", newName)));
        if (newPrice != null) itemDetails.add(Map.entry("price", Map.entry("DOUBLE", String.valueOf(newPrice))));
        if (newQuantity != null) itemDetails.add(Map.entry("quantity", Map.entry("INTEGER", String.valueOf(newQuantity))));
        if (newCategory != null) itemDetails.add(Map.entry("category", Map.entry("TEXT", newCategory)));

        boolean success = DBConnection.updateRow(conn, tableName, constraint, itemDetails);
        SysLogger.logInfo("InventoryDBActions: Update Item - "
                + (success ? "Item updated successfully." : "Item update failed.")
                + " - Item ID: " + id);
        return success;
    }

    // Deletes an item from the inventory.
    public static boolean deleteItem(Connection conn, int id) {
        boolean success = DBConnection.deleteRow(conn, String.valueOf(id), tableName);
        SysLogger.logInfo("InventoryDBActions: Delete Item - "
                + (success ? "Item deleted successfully." : "Item deletion failed.")
                + " - Item ID: " + id);
        return success;
    }

    // Decreases the quantity of a specific item.
    public static boolean decreaseItemQuantity(Connection conn, int itemId, int quantity) {
        Optional<Item> optionalItem = getItem(conn, itemId);
        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            if (item.getQuantity() < quantity) {
                SysLogger.logWarning("InventoryDBActions: Decrease Item Quantity - Not enough stock for item "
                        + item.getName() + " - Item ID: " + itemId);
                return false;
            }
            return updateItemDetails(conn, itemId, null, null, item.getQuantity() - quantity, null);
        }
        return false;
    }

    // Increases the quantity of a specific item.
    public static boolean increaseItemQuantity(Connection conn, int itemId, int quantity) {
        return updateItemDetails(conn, itemId, null, null, quantity, null);
    }
}
