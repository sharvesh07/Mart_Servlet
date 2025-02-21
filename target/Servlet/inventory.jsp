<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Inventory Management</title>
  <style>
    body { font-family: Arial, sans-serif; }
    .container { width: 80%; margin: auto; }
    .center { text-align: center; }
    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: center; }
    .button {
      padding: 8px 16px;
      font-size: 14px;
      background: #8000ff;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }
    .button:hover { background: #00c864; }
    form { margin-top: 20px; }
    label { display: inline-block; width: 100px; margin-bottom: 5px; }
    input[type="text"], input[type="number"] {
      padding: 8px;
      width: 200px;
      margin: 5px 0;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1 class="center">Inventory Management</h1>

    <!-- Display status message if provided -->
    <%
      String status = request.getParameter("status");
      if (status != null) {
    %>
      <p class="center" style="color: green;">Status: <%= status %></p>
    <%
      }
    %>

    <!-- Inventory Table -->
    <h2>Inventory Items</h2>
    <table id="itemTable">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Price</th>
          <th>Quantity</th>
          <th>Description</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
        <%-- Rows will be dynamically populated via JavaScript --%>
      </tbody>
    </table>

    <!-- Form to add a new item -->
    <h2>Add New Item</h2>
    <form action="InventoryServlet" method="post">
      <input type="hidden" name="action" value="add">
      <label for="name">Item Name:</label>
      <input type="text" id="name" name="name" required><br>

      <label for="price">Price:</label>
      <input type="text" id="price" name="price" required><br>

      <label for="quantity">Quantity:</label>
      <input type="number" id="quantity" name="quantity" required><br>

      <label for="description">Description:</label>
      <input type="text" id="description" name="description" required><br>

      <input type="submit" value="Add Item" class="button">
    </form>

    <!-- Form to edit an existing item -->
    <h2>Edit Existing Item</h2>
    <form action="InventoryServlet" method="post">
      <input type="hidden" name="action" value="edit">
      <label for="id">Item ID:</label>
      <input type="text" id="id" name="id" required><br>

      <label for="name_edit">Item Name:</label>
      <input type="text" id="name_edit" name="name" required><br>

      <label for="price_edit">Price:</label>
      <input type="text" id="price_edit" name="price" required><br>

      <label for="quantity_edit">Quantity:</label>
      <input type="number" id="quantity_edit" name="quantity" required><br>

      <label for="description_edit">Description:</label>
      <input type="text" id="description_edit" name="description" required><br>

      <input type="submit" value="Edit Item" class="button">
    </form>

    <div class="center" style="margin-top:20px;">
      <a href="admin.jsp"><button class="button">Back to Admin Menu</button></a>
    </div>
  </div>

  <!-- JavaScript to fetch inventory items via GET and populate the table -->
  <script>
    function fetchItems() {
      fetch('InventoryServlet')
        .then(response => response.json())
        .then(data => {
          const tbody = document.querySelector('#itemTable tbody');
          tbody.innerHTML = ''; // Clear existing rows
          data.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
              <td>${item.id}</td>
              <td>${item.name}</td>
              <td>${item.price}</td>
              <td>${item.quantity}</td>
              <td>${item.description}</td>
              <td>
                <form action="InventoryServlet" method="post" onsubmit="return confirm('Are you sure you want to delete this item?');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="id" value="${item.id}">
                  <input type="submit" value="Delete" class="button">
                </form>
              </td>
            `;
            tbody.appendChild(tr);
          });
        })
        .catch(error => console.error('Error fetching items:', error));
    }

    document.addEventListener('DOMContentLoaded', fetchItems);
  </script>
</body>
</html>
