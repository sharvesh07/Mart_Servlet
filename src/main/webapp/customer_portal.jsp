<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="com.ObjectClass.Orders" %>
<%@ page import="com.ObjectClass.User" %>
<%@ page import="com.ObjectClass.Item" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Database.Connection.DBConnection" %>
<%@ page import="Database.DBOperations.CustomerDBAction" %>
<%@ page import="Database.DBOperations.InventoryDBActions" %>
<%@ page import="Database.Jedis.InventoryCache" %>
<%@ page import="Database.Jedis.CustomerCache" %>

<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection connection = null;
    try {
        connection = DBConnection.getConnection();
        String action = request.getParameter("action");
        if (action == null) action = "home";
        String status = request.getParameter("status");

        // Retrieve inventory using cache-aside
        List<Item> inventory = InventoryCache.getCachedInventory();
        if (inventory == null) {
            inventory = InventoryDBActions.getAllItems(connection);
            InventoryCache.cacheInventory(inventory);
        }

        // Build a set of categories for filtering.
        Set<String> categorySet = new HashSet<>();
        for (Item item : inventory) {
            categorySet.add(item.getCategory());
        }

        // Retrieve cart items using cache-aside.
        List<Item> cartItems = CustomerCache.getCachedCartItems(user.getId());
        if (cartItems == null) {
            cartItems = CustomerDBAction.getCurrentCartItems(connection, user.getId());
            CustomerCache.cacheCartItems(user.getId(), cartItems);
        }

        // Retrieve "Saved for Later" items using cache-aside.
        List<Item> savedForLaterItems = CustomerCache.getCachedSavedForLaterItems(user.getId());
        if (savedForLaterItems == null) {
            savedForLaterItems = CustomerDBAction.getSavedForLaterItems(connection, user.getId());
            CustomerCache.cacheSavedForLaterItems(user.getId(), savedForLaterItems);
        }

        // Previous orders are fetched directly from the DB.
        List<Orders> previousOrders = CustomerDBAction.getPreviousOrders(connection, user.getId());
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Customer Portal</title>
    <link rel="stylesheet" href="css/customer.css">
</head>
<body>
    <nav class="navbar">
        <div class="logo">MyShop</div>
    </nav>

    <div class="container">
        <aside class="sidebar">
            <div class="sidebar-buttons">
                <button onclick="location.href='customer_portal.jsp?action=home'">Home</button>
                <button onclick="location.href='customer_portal.jsp?action=profile'">Profile</button>
                <button onclick="location.href='customer_portal.jsp?action=cart'">Cart</button>
                <button onclick="location.href='UserServlet?action=logout'">Logout</button>
            </div>

            <% if ("home".equals(action)) { %>
            <div class="search-bar">
                <input type="text" placeholder="Search products..." id="searchInput" aria-label="Search products">
                <button class="search-button" onclick="searchProducts()">
                    <i class="fas fa-search"></i>
                </button>
            </div>

            <div class="category-filter">
                <h3>Filter by Category</h3>
                <% for (String category : categorySet) { %>
                <label>
                    <input type="checkbox" name="category" value="<%= category %>" checked>
                    <%= category %>
                </label>
                <% } %>
            </div>
            <% } %>
        </aside>

        <main class="main-content">
            <% if (status != null) { %>
            <div class="status-message"><%= status %></div>
            <% } %>

            <% if ("profile".equals(action)) { %>
            <h2>My Profile</h2>
            <div class="profile-info">
                <p><strong>Name:</strong> <%= user.getName() %></p>
                <p><strong>Wallet Balance:</strong> <%= CustomerDBAction.getDiscountPoints(connection, user.getId()) %> P</p>

                <h3>Previous Orders</h3>
                <% if (previousOrders.isEmpty()) { %>
                    <p>No previous orders found.</p>
                <% } else { %>
                    <table class="orders-table">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Date</th>
                                <th>Time</th>
                                <th>Total</th>
                                <th>Item</th>
                                <th>Quantity</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Orders order : previousOrders) {
                                Map<String, Integer> items = order.getCart();
                                if (items.isEmpty()) continue;
                                List<Map.Entry<String, Integer>> entries = new ArrayList<>(items.entrySet());
                            %>
                                <tr>
                                    <td rowspan="<%= entries.size() %>"><%= order.getTransactionId() %></td>
                                    <td rowspan="<%= entries.size() %>"><%= order.getDate() %></td>
                                    <td rowspan="<%= entries.size() %>"><%= order.getTime() %></td>
                                    <td rowspan="<%= entries.size() %>">$<%= String.format("%.2f", order.getTotalPrice()) %></td>
                                    <td><%= entries.get(0).getKey() %></td>
                                    <td><%= entries.get(0).getValue() %></td>
                                </tr>
                                <% for (int i = 1; i < entries.size(); i++) { %>
                                    <tr>
                                        <td><%= entries.get(i).getKey() %></td>
                                        <td><%= entries.get(i).getValue() %></td>
                                    </tr>
                                <% } %>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>

                <h3>Update Profile</h3>
                <form action="CustomerServlet" method="post">
                    <input type="hidden" name="action" value="editProfile">
                    <input type="text" name="name" placeholder="New name" required>
                    <input type="password" name="password" placeholder="New password">
                    <button type="submit" class="add-to-cart-btn">Update</button>
                </form>
            </div>

            <% } else if ("cart".equals(action)) { %>
            <h2>My Cart</h2>
            <div class="product-grid">
                <% if (cartItems.isEmpty()) { %>
                    <p class="empty-cart">Your cart is empty.</p>
                <% } else { %>
                    <% for (Item item : cartItems) {
                        int quantityInCart = CustomerDBAction.getCartItemQuantity(connection, user.getId(), item.getId());
                    %>
                    <div class="product-card">
                        <div class="product-content">
                            <div class="product-info">
                                <h4><%= item.getName() %></h4>
                                <p>Category: <%= item.getCategory() %></p>
                            </div>
                            <div class="product-meta">
                                <p> Price: $<%= item.getPrice() %></p>
                                <p> Quantity: <%= quantityInCart %></p>
                            </div>
                            <div class="product-actions">
                                <form action="CustomerServlet" method="post">
                                    <input type="hidden" name="action" value="updateCart">
                                    <input type="hidden" name="itemId" value="<%= item.getId() %>">
                                    <input type="number" name="quantity" value="<%= quantityInCart %>" min="0" max="<%= item.getQuantity() %>" required>
                                    <button type="submit" class="add-to-cart-btn">Update</button>
                                </form>

                                <form action="CustomerServlet" method="post" onsubmit="return confirm('Are you sure?');">
                                    <input type="hidden" name="action" value="removeFromCart">
                                    <input type="hidden" name="itemId" value="<%= item.getId() %>">
                                    <button type="submit" class="add-to-cart-btn">Remove</button>
                                </form>

                                <form action="CustomerServlet" method="post">
                                    <input type="hidden" name="action" value="saveForLater">
                                    <input type="hidden" name="itemId" value="<%= item.getId() %>">
                                    <button type="submit" class="add-to-cart-btn">Save for Later</button>
                                </form>

                                <form action="CustomerServlet" method="post">
                                    <input type="hidden" name="action" value="moveToCart">
                                    <input type="hidden" name="itemId" value="<%= item.getId() %>">
                                    <button type="submit" class="add-to-cart-btn">Move to Cart</button>
                                </form>
                            </div>
                        </div>
                    </div>
                    <% } %>
                <% } %>
            </div>
            <div class="cart-actions">
                <p>Total: $<%= CustomerDBAction.computeCartTotalPrice(connection, user.getId()) %></p>
                <form action="CustomerServlet" method="post">
                    <input type="hidden" name="action" value="checkout">
                    <button type="submit" class="checkout-btn">Checkout</button>
                </form>
            </div>

            <h3>Saved for Later</h3>
            <div class="product-grid">
                <% if (savedForLaterItems.isEmpty()) { %>
                    <p>No items saved for later.</p>
                <% } else { %>
                    <% for (Item item : savedForLaterItems) { %>
                        <div class="product-card">
                            <h4><%= item.getName() %></h4>
                            <p>Price: $<%= item.getPrice() %></p>
                            <form action="CustomerServlet" method="post">
                                <input type="hidden" name="action" value="moveToCart">
                                <input type="hidden" name="itemId" value="<%= item.getId() %>">
                                <button type="submit" class="add-to-cart-btn">Move to Cart</button>
                            </form>
                        </div>
                    <% } %>
                <% } %>
            </div>

            <% } else { %>
            <h2>Available Products</h2>
            <div class="product-grid">
                <% for (Item item : inventory) { %>
                <div class="product-card" data-category="<%= item.getCategory() %>" data-name="<%= item.getName() %>">
                    <h4><%= item.getName() %></h4>
                    <p>Price: $<%= item.getPrice() %></p>
                    <p>Stock: <%= item.getQuantity() %></p>
                    <form action="CustomerServlet" method="post">
                        <input type="hidden" name="action" value="addToCart">
                        <input type="hidden" name="itemId" value="<%= item.getId() %>">
                        <input type="number" name="quantity" value="1" min="1" max="<%= item.getQuantity() %>" required>
                        <button type="submit" class="add-to-cart-btn">Add to Cart</button>
                    </form>
                </div>
                <% } %>
            </div>
            <% } %>
        </main>
    </div>

    <script>
        document.querySelectorAll('.category-filter input').forEach(checkbox => {
            checkbox.addEventListener('change', filterProducts);
        });

        function filterProducts() {
            const checkedCategories = Array.from(document.querySelectorAll('.category-filter input:checked'))
                .map(cb => cb.value);

            document.querySelectorAll('.product-card').forEach(card => {
                const category = card.dataset.category;
                card.style.display = checkedCategories.includes(category) ? 'block' : 'none';
            });
        }

        function searchProducts() {
            const term = document.getElementById('searchInput').value.toLowerCase();
            document.querySelectorAll('.product-card').forEach(card => {
                const name = card.dataset.name.toLowerCase();
                card.style.display = name.includes(term) ? 'block' : 'none';
            });
        }
    </script>
</body>
</html>

<%
    } catch (Exception e) {
        e.printStackTrace(); // Consider logging the error
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
    } finally {
        if (connection != null) {
            try {
                connection.close(); // Return the connection to the pool
            } catch (SQLException e) {
                e.printStackTrace(); // Consider logging the error
            }
        }
    }
%>
