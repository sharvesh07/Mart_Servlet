<!-- edit_inventory.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="header.jsp" />
</head>
<body>
    <div class="center-box">
        <h2>Edit Inventory</h2>
        <h3>Item Table</h3>
        <p>[Inventory items will be listed here]</p>
        <form action="InventoryServlet" method="post">
            <label for="item_id">Item Id:</label>
            <input type="text" id="item_id" name="item_id" required><br><br>

            <label for="item_name">Item Name:</label>
            <input type="text" id="item_name" name="item_name" required><br><br>

            <label for="item_price">Price:</label>
            <input type="text" id="item_price" name="item_price" required><br><br>

            <label for="item_quantity">Quantity:</label>
            <input type="text" id="item_quantity" name="item_quantity" required><br><br>

            <label for="item_description">Description:</label>
            <input type="text" id="item_description" name="item_description" required><br><br>

            <input type="submit" value="Edit Item" class="button">
        </form>
    </div>
    <div class="center">
        <a href="inventory.jsp" class="button">Back</a>
    </div>
</body>
</html>
