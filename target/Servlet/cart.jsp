<%@ page import="java.util.List, com.ObjectClass.Item" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Your Shopping Cart</title>
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
        .button:hover { background: #00c864; }
        a { text-decoration: none; }
    </style>
</head>
<body>
    <div class="container">
        <h2 class="center">Your Shopping Cart</h2>
        <%
            // Retrieve the cart items that were set by the CustomerServlet.
            List<Item> cartItems = (List<Item>) request.getAttribute("cartItems");
        %>
        <table>
            <tr>
                <th>Item Name</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Action</th>
            </tr>
            <%
                if (cartItems != null && !cartItems.isEmpty()) {
                    for (Item item : cartItems) {
                        // Here, you might have a method to retrieve the specific quantity for the cart item.
                        int quantity = 1; // Replace with actual quantity retrieval if available.
            %>
            <tr>
                <td><%= item.getName() %></td>
                <td><%= item.getPrice() %></td>
                <td><%= quantity %></td>
                <td>
                    <form action="CustomerServlet" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="removeFromCart">
                        <input type="hidden" name="itemId" value="<%= item.getId() %>">
                        <input type="submit" value="Remove" class="button">
                    </form>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="4">Your cart is empty.</td>
            </tr>
            <%
                }
            %>
        </table>
        <div class="center">
            <form action="CustomerServlet" method="post">
                <input type="hidden" name="action" value="checkout">
                <input type="submit" value="Checkout" class="button">
            </form>
        </div>
        <br>
        <div class="center">
            <a href="index.jsp"><button class="button">Back to Home</button></a>
        </div>
    </div>
</body>
</html>
