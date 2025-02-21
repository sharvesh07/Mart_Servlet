<%@ page import="java.util.List, com.ObjectClass.Item" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Customer Cart</title>
    <style>
        .container {
            width: 80%;
            margin: auto;
            font-family: Arial, sans-serif;
        }
        .center {
            text-align: center;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 10px;
            text-align: center;
        }
        .button {
            padding: 10px 20px;
            font-size: 16px;
            background: #8000ff;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        .button:hover {
            background: #00c864;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2 class="center">Your Shopping Cart</h2>
        <%
            String action = request.getParameter("action");
            if ("cart".equals(action)) {
                List<Item> cartItems = (List<Item>) request.getAttribute("cartItems");
                if (cartItems != null && !cartItems.isEmpty()) {
        %>
        <table>
            <tr>
                <th>Item Name</th>
                <th>Price</th>
                <th>Action</th>
            </tr>
            <% for (Item item : cartItems) { %>
            <tr>
                <td><%= item.getName() %></td>
                <td><%= item.getPrice() %></td>
                <td>
                    <form action="CustomerServlet" method="post">
                        <input type="hidden" name="action" value="removeFromCart">
                        <input type="hidden" name="itemId" value="<%= item.getId() %>">
                        <input type="submit" value="Remove" class="button">
                    </form>
                </td>
            </tr>
            <% } %>
        </table>
        <%
                } else {
        %>
            <p class="center">Your cart is empty.</p>
        <%
                }
            } else {
        %>
            <p class="center">No cart action specified.</p>
        <%
            }
        %>
        <div class="center" style="margin-top:20px;">
            <a href="index.jsp"><button class="button">Back to Home</button></a>
        </div>
    </div>
</body>
</html>
