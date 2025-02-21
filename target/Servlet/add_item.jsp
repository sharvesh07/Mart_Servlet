<!-- add_item.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="header.jsp" />
</head>
<body>
    <div class="center-box">
        <h2>New Item Page</h2>
        <form action="InventoryServlet" method="post">
            <label for="add_item_name">Item Name:</label>
            <input type="text" id="add_item_name" name="item_name" required><br><br>

            <label for="add_price">Price:</label>
            <input type="text" id="add_price" name="price" required><br><br>

            <label for="add_quantity">Quantity:</label>
            <input type="text" id="add_quantity" name="quantity" required><br><br>

            <label for="add_description">Description:</label>
            <input type="text" id="add_description" name="description" required><br><br>

            <input type="submit" value="Add Item" class="button">
        </form>
    </div>
    <div class="center">
        <a href="inventory.jsp" class="button">Back</a>
    </div>
</body>
</html>
