package com.Servlets;

import Database.DBOperations.InventoryDBActions;
import Database.Connection.DBConnection;
import Database.Cache.InventoryCache;
import com.ObjectClass.Item;
import com.ObjectClass.User;
import com.google.gson.Gson;
import com.System.SysLogger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.*;

@WebServlet("/InventoryServlet")
public class InventoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection conn = DBConnection.getConnection()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            // Cache-Aside: Try to get the full inventory from Redis.
            List<Item> items = InventoryCache.getCachedInventory();
            if (items == null) {
                // Cache miss: load from database and cache the result.
                items = InventoryDBActions.getAllItems(conn);
                InventoryCache.cacheInventory(items);
            }

            String json = new Gson().toJson(items);
            out.print(json);
            out.flush();
        } catch (Exception e) {
            SysLogger.logSevere("InventoryServlet: Database error during GET request - Error: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection conn = DBConnection.getConnection()) {
            String action = request.getParameter("action");
            switch (action) {
                case "add":
                    addItem(request, response, conn);
                    break;
                case "edit":
                    editItem(request, response, conn);
                    break;
                case "delete":
                    deleteItem(request, response, conn);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
                    break;
            }
        } catch (Exception e) {
            SysLogger.logSevere("InventoryServlet: Database error during POST request - Error: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void addItem(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        String name = request.getParameter("name");
        int quantity = parseQuantity(request, response);
        if (quantity == -1) return; // Invalid quantity

        double price = parsePrice(request, response);
        if (price == -1) return; // Invalid price

        String category = request.getParameter("category");
        boolean success = InventoryDBActions.addItem(conn, name, price, quantity, category);

        SysLogger.logInfo("InventoryServlet: Add Item - " +
                (success ? "Item added successfully!" : "Item addition failed.") +
                " - Item Name: " + name);

        // Write-Through: Invalidate the cache so it is repopulated on next GET.
        if (success) {
            InventoryCache.clearInventoryCache();
        }
        redirectToInventoryPage(request, response, success);
    }

    private void editItem(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        int quantity = parseQuantity(request, response);
        if (quantity == -1) return; // Invalid quantity

        double price = parsePrice(request, response);
        if (price == -1) return; // Invalid price

        String category = request.getParameter("category");
        boolean success = InventoryDBActions.updateItemDetails(conn, id, name, price, quantity, category);

        SysLogger.logInfo("InventoryServlet: Edit Item - " +
                (success ? "Item updated successfully." : "Item update failed.") +
                " - Item ID: " + id);

        // Write-Through: Invalidate the cache on a successful update.
        if (success) {
            InventoryCache.clearInventoryCache();
        }
        redirectToInventoryPage(request, response, success);
    }

    private void deleteItem(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        boolean success = InventoryDBActions.deleteItem(conn, id);

        SysLogger.logInfo("InventoryServlet: Delete Item - " +
                (success ? "Item deleted successfully." : "Item deletion failed.") +
                " - Item ID: " + id);

        // Write-Through: Invalidate the cache on a successful deletion.
        if (success) {
            InventoryCache.clearInventoryCache();
        }
        redirectToInventoryPage(request, response, success);
    }

    private void redirectToInventoryPage(HttpServletRequest request, HttpServletResponse response, boolean success) throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        // Redirect to different pages for employees vs. regular users.
        String redirectPage = (user != null && "employee".equals(user.getAccess())) ? "emp_inventory.jsp" : "inventory.jsp";
        response.sendRedirect(redirectPage + "?status=" + (success ? "success" : "failure"));
    }

    private int parseQuantity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String quantityParam = request.getParameter("quantity");
        try {
            return Integer.parseInt(quantityParam);
        } catch (NumberFormatException e) {
            SysLogger.logWarning("InventoryServlet: Parse Quantity - Invalid quantity: " + quantityParam + " - Error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid quantity.");
            return -1;
        }
    }

    private double parsePrice(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String priceParam = request.getParameter("price");
        try {
            return Double.parseDouble(priceParam);
        } catch (NumberFormatException e) {
            SysLogger.logWarning("InventoryServlet: Parse Price - Invalid price: " + priceParam + " - Error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid price.");
            return -1;
        }
    }
}
