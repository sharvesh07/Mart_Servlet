package com.Servlets;

import Database.DBOperations.CustomerDBAction;
import Database.Connection.DBConnection;
import Database.DBOperations.InventoryDBActions;
import Database.DBOperations.UserDBActions;
import Database.Cache.CustomerCache;
import com.ObjectClass.Item;
import com.ObjectClass.Orders;
import com.ObjectClass.User;
import com.google.gson.Gson;
import com.System.SysLogger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/CustomerServlet")
public class CustomerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection conn = DBConnection.getConnection()) {
            handleGetRequest(request, response, conn);
        } catch (SQLException e) {
            SysLogger.logSevere("CustomerServlet: Database error during GET request - Error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection conn = DBConnection.getConnection()) {
            handlePostRequest(request, response, conn);
        } catch (SQLException e) {
            SysLogger.logSevere("CustomerServlet: Database error during POST request - Error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void handleGetRequest(HttpServletRequest request, HttpServletResponse response, Connection conn)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = Optional.ofNullable(request.getParameter("action")).orElse("home");
        String status = request.getParameter("status");

        // Fetch inventory from DB.
        List<Item> inventory = InventoryDBActions.getAllItems(conn);

        // Cache-Aside: Retrieve cart items.
        List<Item> cartItems = CustomerCache.getCachedCartItems(user.getId());
        if (cartItems == null) {
            cartItems = CustomerDBAction.getCurrentCartItems(conn, user.getId());
            CustomerCache.cacheCartItems(user.getId(), cartItems);
        }

        // Cache-Aside: Retrieve saved-for-later items.
        List<Item> savedForLaterItems = CustomerCache.getCachedSavedForLaterItems(user.getId());
        if (savedForLaterItems == null) {
            savedForLaterItems = CustomerDBAction.getSavedForLaterItems(conn, user.getId());
            CustomerCache.cacheSavedForLaterItems(user.getId(), savedForLaterItems);
        }

        // Retrieve previous orders directly (caching not applied here).
        List<Orders> previousOrders = CustomerDBAction.getPreviousOrders(conn, user.getId());

        // Cache-Aside: Retrieve wallet discount points.
        Integer walletPoints = CustomerCache.getCachedWalletPoints(user.getId());
        if (walletPoints == null) {
            walletPoints = CustomerDBAction.getDiscountPoints(conn, user.getId());
            CustomerCache.cacheWalletPoints(user.getId(), walletPoints);
        }

        // Set attributes for JSP.
        request.setAttribute("user", user);
        request.setAttribute("inventory", inventory);
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("savedForLaterItems", savedForLaterItems);
        request.setAttribute("previousOrders", previousOrders);
        request.setAttribute("walletPoints", walletPoints);
        request.setAttribute("status", status);
        request.setAttribute("action", action);

        request.getRequestDispatcher("customer_portal.jsp").forward(request, response);
    }

    private void handlePostRequest(HttpServletRequest request, HttpServletResponse response, Connection conn)
            throws IOException, ServletException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("CustomerServlet?action=profile");
            return;
        }
        switch (action.trim()) {
            case "editProfile":
                editProfile(request, response, conn);
                break;
            case "addToCart":
                addToCart(request, response, conn);
                break;
            case "removeFromCart":
                removeFromCart(request, response, conn);
                break;
            case "checkout":
                checkout(request, response, conn);
                break;
            case "deleteAccount":
                deleteAccount(request, response, conn);
                break;
            case "saveForLater":
                saveForLater(request, response, conn);
                break;
            case "moveToCart":
                moveToCart(request, response, conn);
                break;
            case "list":
                listCustomers(request, response, conn);
                break;
            default:
                response.sendRedirect("CustomerServlet?action=profile");
                break;
        }
    }

    private void saveForLater(HttpServletRequest request, HttpServletResponse response, Connection conn)
            throws IOException {
        User user = getUserFromSession(request, response);
        if (user == null) return;
        int itemId = parseItemId(request, response);
        if (itemId == -1) return;

        CustomerDBAction.saveItemForLater(conn, user.getId(), itemId);
        // Write-Through: Refresh saved-for-later cache.
        List<Item> savedItems = CustomerDBAction.getSavedForLaterItems(conn, user.getId());
        CustomerCache.cacheSavedForLaterItems(user.getId(), savedItems);
        SysLogger.logInfo("CustomerServlet: Save For Later - Item ID: " + itemId + ", User ID: " + user.getId());
        response.sendRedirect("customer_portal.jsp?action=cart");
    }

    private void moveToCart(HttpServletRequest request, HttpServletResponse response, Connection conn)
            throws IOException {
        User user = getUserFromSession(request, response);
        if (user == null) return;
        int itemId = parseItemId(request, response);
        if (itemId == -1) return;

        CustomerDBAction.moveToCart(conn, user.getId(), itemId);
        // Write-Through: Refresh both cart and saved-for-later caches.
        List<Item> updatedCart = CustomerDBAction.getCurrentCartItems(conn, user.getId());
        CustomerCache.cacheCartItems(user.getId(), updatedCart);
        List<Item> savedItems = CustomerDBAction.getSavedForLaterItems(conn, user.getId());
        CustomerCache.cacheSavedForLaterItems(user.getId(), savedItems);
        SysLogger.logInfo("CustomerServlet: Move To Cart - Item ID: " + itemId + ", User ID: " + user.getId());
        response.sendRedirect("customer_portal.jsp?action=cart");
    }

    private void listCustomers(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        List<User> customers = UserDBActions.getAllCustomers(conn);
        List<Map<String, Object>> customerDetails = new ArrayList<>();
        for (User u : customers) {
            Map<String, Object> customerMap = new HashMap<>();
            customerMap.put("id", u.getId());
            customerMap.put("name", u.getName());
            customerMap.put("password", u.getPassword());
            double cartTotal = CustomerDBAction.computeCartTotalPrice(conn, u.getId());
            customerMap.put("cartTotal", cartTotal);
            int wallet = CustomerDBAction.getDiscountPoints(conn, u.getId());
            customerMap.put("wallet", wallet);
            customerDetails.add(customerMap);
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(customerDetails));
        SysLogger.logInfo("CustomerServlet: List Customers - Returned " + customers.size() + " customers.");
    }

    private void editProfile(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        User user = getUserFromSession(request, response);
        if (user == null) return;

        String newName = request.getParameter("name");
        String newPassword = request.getParameter("password");

        if (newName != null && !newName.isEmpty()) {
            user.setName(newName);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }
        UserDBActions.updateUser(conn, user);
        request.getSession().setAttribute("user", user);
        SysLogger.logInfo("CustomerServlet: Edit Profile - Profile updated. User ID: " + user.getId());
        response.sendRedirect("CustomerServlet?action=profile&status=updated");
    }

    private void addToCart(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        User user = getUserFromSession(request, response);
        if (user == null) return;
        int itemId = parseItemId(request, response);
        if (itemId == -1) return;
        int quantity = parseQuantity(request, response);
        if (quantity <= 0) return;

        Optional<Item> itemOpt = InventoryDBActions.getItem(conn, itemId);
        if (itemOpt.isEmpty() || itemOpt.get().getQuantity() < quantity) {
            response.sendRedirect("customer_portal.jsp?action=home&status=not_enough_stock");
            return;
        }

        Item item = itemOpt.get();
        // Decrease inventory quantity by the amount added to cart.
        InventoryDBActions.decreaseItemQuantity(conn, item.getId(), quantity);

        if (CustomerDBAction.isItemInCart(conn, user.getId(), itemId)) {
            int currentQuantity = CustomerDBAction.getCartItemQuantity(conn, user.getId(), itemId);
            CustomerDBAction.updateCartItemQuantity(conn, user.getId(), itemId, currentQuantity + quantity);
        } else {
            CustomerDBAction.addToCart(conn, user.getId(), itemId, quantity);
        }
        // Write-Through: Refresh the cart cache after updating the cart.
        List<Item> updatedCart = CustomerDBAction.getCurrentCartItems(conn, user.getId());
        CustomerCache.cacheCartItems(user.getId(), updatedCart);
        SysLogger.logInfo("CustomerServlet: Add To Cart - Item ID: " + itemId + ", Quantity: " + quantity + ", User ID: " + user.getId());
        response.sendRedirect("customer_portal.jsp?action=cart");
    }

    private void removeFromCart(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        User user = getUserFromSession(request, response);
        if (user == null) return;
        int itemId = parseItemId(request, response);
        if (itemId == -1) return;

        int quantityInCart = CustomerDBAction.getCartItemQuantity(conn, user.getId(), itemId);
        if (quantityInCart <= 0) {
            response.sendRedirect("CustomerServlet?action=cart&status=not_found");
            return;
        }

        boolean removed = CustomerDBAction.removeFromCart(conn, user.getId(), itemId);
        if (removed) {
            InventoryDBActions.increaseItemQuantity(conn, itemId, quantityInCart);
            // Write-Through: Refresh the cart cache after removal.
            List<Item> updatedCart = CustomerDBAction.getCurrentCartItems(conn, user.getId());
            CustomerCache.cacheCartItems(user.getId(), updatedCart);
            SysLogger.logInfo("CustomerServlet: Remove From Cart - Item ID: " + itemId + ", User ID: " + user.getId());
            response.sendRedirect("CustomerServlet?action=cart&status=removed");
        } else {
            response.sendRedirect("CustomerServlet?action=cart&status=error");
        }
    }

    private void checkout(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        User user = getUserFromSession(request, response);
        if (user == null) return;

        List<Item> cartItems = CustomerDBAction.getCurrentCartItems(conn, user.getId());
        if (cartItems.isEmpty()) {
            response.sendRedirect("CustomerServlet?action=cart&status=empty_cart");
            return;
        }

        int totalPrice = cartItems.stream().mapToInt(item -> (int) (item.getPrice() * item.getQuantity())).sum();
        int discountPoints = CustomerCache.getCachedWalletPoints(user.getId()) != null ?
                CustomerCache.getCachedWalletPoints(user.getId()) : CustomerDBAction.getDiscountPoints(conn, user.getId());

        // Apply discount if enough discount points are available.
        if (discountPoints > 10) {
            totalPrice = (int) (totalPrice * 0.9);
            discountPoints -= 10;
            CustomerDBAction.updateDiscountPoints(conn, user.getId(), discountPoints);
        }

        discountPoints += totalPrice / 100;  // Earn additional points.
        int orderId = DBConnection.getNextTransactionId(conn, "Order_Header");
        CustomerDBAction.orderTransaction(conn, orderId, user.getId(), totalPrice);
        CustomerDBAction.transferCartItemsToOrderDetails(conn, orderId, user.getId());
        CustomerDBAction.updateDiscountPoints(conn, user.getId(), discountPoints);
        // Write-Through: Update wallet cache after discount points change.
        CustomerCache.cacheWalletPoints(user.getId(), discountPoints);
        CustomerDBAction.clearCart(conn, user.getId());
        // Write-Through: Clear the cart cache after checkout.
        CustomerCache.clearCartCache(user.getId());
        SysLogger.logInfo("CustomerServlet: Checkout - Order ID: " + orderId + ", Total Price: " + totalPrice + ", User ID: " + user.getId());
        response.sendRedirect("CustomerServlet?action=cart&status=checkout_success");
    }

    private void deleteAccount(HttpServletRequest request, HttpServletResponse response, Connection conn) throws IOException {
        User user = getUserFromSession(request, response);
        if (user == null) return;

        UserDBActions.deleteUser(conn, user.getId());
        request.getSession().invalidate();
        SysLogger.logInfo("CustomerServlet: Delete Account - User account deleted. User ID: " + user.getId());
        response.sendRedirect("index.jsp?status=account_deleted");
    }

    private User getUserFromSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("login.jsp");
        }
        return user;
    }

    private int parseItemId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return Integer.parseInt(request.getParameter("itemId"));
        } catch (NumberFormatException e) {
            response.sendRedirect("customer_portal.jsp?action=home&status=invalid_item");
            return -1;
        }
    }

    private int parseQuantity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return Integer.parseInt(request.getParameter("quantity"));
        } catch (NumberFormatException e) {
            response.sendRedirect("customer_portal.jsp?action=home&status=invalid_quantity");
            return -1;
        }
    }
}
