
package com.ObjectClass;

import Database.Connection.DBConnection;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Orders {
    private int transactionId;
    private int userId;
    private String date;
    private String time;
    private double totalPrice;
    private Map<String, Integer> cart;
    private static final Logger LOGGER = Logger.getLogger(Orders.class.getName());

    public Orders(int transactionId, int userId, String date, String time, double totalPrice) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.totalPrice = totalPrice;
        this.cart = new HashMap<>();
    }

    public int getTransactionId() { return transactionId; }
    public double getTotalPrice() { return totalPrice; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public Map<String, Integer> getCart() {
        return this.cart;
    }

    public static List<Orders> loadOldOrders(Connection conn, int userId) {
        List<Orders> ordersList = new ArrayList<>();
        String query = "SELECT oh.transaction_id, oh.total_price, oh.timestamp, i.name, od.quantity FROM Order_Header oh " +
                "JOIN Order_Details od ON oh.transaction_id = od.transaction_id " +
                "JOIN Inventory i ON od.item_id = i.id " +
                "WHERE oh.user_id = ? ORDER BY oh.transaction_id";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            Map<Integer, Orders> ordersMap = new HashMap<>();

            while (rs.next()) {
                int transId = rs.getInt("transaction_id");
                double totalPrice = rs.getDouble("total_price");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                String date = timestamp.toLocalDateTime().toLocalDate().toString();
                String time = timestamp.toLocalDateTime().toLocalTime().toString();
                String itemName = rs.getString("name");
                int quantity = rs.getInt("quantity");

                ordersMap.putIfAbsent(transId, new Orders(transId, userId, date, time, totalPrice));
                ordersMap.get(transId).cart.put(itemName, quantity);
            }

            ordersList.addAll(ordersMap.values());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading orders", e);
        }
        return ordersList;
    }
}
