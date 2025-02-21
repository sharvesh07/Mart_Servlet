<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Inventory Management</title>
    <link rel="stylesheet" type="text/css" href="css/inventory.css">
</head>
<body>
    <div class="container">
        <h1>Admin Inventory Management</h1>

        <h2>Inventory Items</h2>
        <table id="itemTable">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Category</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>

        <h2>Add New Item</h2>
        <form id="addForm" action="InventoryServlet" method="post">
            <input type="hidden" name="action" value="add">
            <label for="add-name">Item Name:</label>
            <input type="text" id="add-name" name="name" required><br>

            <label for="add-price">Price:</label>
            <input type="text" id="add-price" name="price" required><br>

            <label for="add-quantity">Quantity:</label>
            <input type="number" id="add-quantity" name="quantity" required><br>

            <label for="add-category">Category:</label>
            <input type="text" id="add-category" name="category" required><br>

            <input type="submit" value="Add Item" class="button">
        </form>

        <h2>Edit Item</h2>
        <form id="editForm" action="InventoryServlet" method="post">
            <input type="hidden" name="action" value="edit">
            <input type="hidden" id="edit-id" name="id">
            <label for="edit-name">Item Name:</label>
            <input type="text" id="edit-name" name="name" required><br>

            <label for="edit-price">Price:</label>
            <input type="text" id="edit-price" name="price" required><br>

            <label for="edit-quantity">Quantity:</label>
            <input type="number" id="edit-quantity" name="quantity" required><br>

            <label for="edit-category">Category:</label>
            <input type="text" id="edit-category" name="category" required><br>

            <input type="submit" value="Update Item" class="button">
        </form>

        <div style="margin-top:20px;">
            <a href="admin.jsp"><button class="button">Back to Admin Panel</button></a>
        </div>
    </div>

    <script>
        function fetchItems() {
            fetch('InventoryServlet')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#itemTable tbody');
                    tbody.innerHTML = '';
                    data.forEach(item => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td>${item.id}</td>
                            <td>${item.name}</td>
                            <td>${item.price}</td>
                            <td>${item.quantity}</td>
                            <td>${item.category}</td>
                            <td>
                                <button class="button" onclick="editItem(${item.id}, '${item.name}', '${item.price}', ${item.quantity}, '${item.category}')">Edit</button>
                                <form action="InventoryServlet" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${item.id}">
                                    <input type="submit" value="Delete" class="button" onclick="return confirm('Are you sure?');">
                                </form>
                            </td>
                        `;
                        tbody.appendChild(tr);
                    });
                })
                .catch(error => console.error('Error fetching items:', error));
        }

        function editItem(id, name, price, quantity, category) {
            document.getElementById('edit-id').value = id;
            document.getElementById('edit-name').value = name;
            document.getElementById('edit-price').value = price;
            document.getElementById('edit-quantity').value = quantity;
            document.getElementById('edit-category').value = category;
        }

        document.addEventListener('DOMContentLoaded', fetchItems);
    </script>
</body>
</html>
