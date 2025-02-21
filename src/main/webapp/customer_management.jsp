<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Customer Management</title>
    <style>
        /* Previous CSS styles remain unchanged */
        body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #6e7bff, #ff6e7b); margin: 0; padding: 0; color: white; }
        .container { width: 80%; margin: auto; background: #fff; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); color: #333; }
        .center { text-align: center; }
        .button { padding: 10px 20px; background: #8000ff; color: white; border: none; border-radius: 5px; cursor: pointer; transition: background 0.3s; }
        .button:hover { background: #00c864; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }
        th { background-color: #8000ff; color: white; }
        .loading { text-align: center; color: #ff6347; display: none; }
    </style>
</head>

<body>
    <div class="container">
        <h1 class="center">Customer Management</h1>

        <%-- Status message display --%>
        <% if (request.getParameter("status") != null) { %>
            <p class="center" style="color: green;">Status: <%= request.getParameter("status") %></p>
        <% } %>

        <h2 class="center">Customer List</h2>
        <div class="loading" id="loading">Loading customers...</div>
        <table id="customerTable">
            <thead>
                <tr>
                    <th>ID</th><th>Name</th><th>Password</th>
                    <th>Cart Total</th><th>Wallet Points</th><th>Action</th>
                </tr>
            </thead>
            <tbody><!-- Dynamic content --></tbody>
        </table>

        <h2>Add New Customer</h2>
        <form action="CustomerServlet" method="post" id="addCustomerForm">
            <input type="hidden" name="action" value="add">
            <label>Name: <input type="text" name="name" required></label>
            <label>Password: <input type="password" name="password" required></label>
            <input type="submit" value="Add Customer" class="button">
        </form>

        <div class="center" style="margin-top:20px;">
            <a href="admin.jsp"><button class="button">Back to Admin Menu</button></a>
        </div>
    </div>

    <script>
        // Enhanced fetch function with error handling
        function fetchCustomers() {
            const loading = document.getElementById('loading');
            loading.style.display = 'block';

            fetch('CustomerServlet?action=list')
                .then(response => {
                    if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
                    return response.json();
                })
                .then(data => {
                    console.log('Received data:', data);
                    const tbody = document.querySelector('#customerTable tbody');
                    tbody.innerHTML = '';

                    if (data.length === 0) {
                        showMessage('No customers found');
                        return;
                    }

                    data.forEach(customer => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td>${customer.id}</td>
                            <td>${customer.name}</td>
                            <td>${customer.password}</td>
                            <td>$${(customer.cartTotal || 0).toFixed(2)}</td>
                            <td>${customer.wallet || 0}</td>
                            <td>
                                <form action="CustomerServlet" method="post"
                                      onsubmit="return confirm('Delete customer ${customer.name}?');">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${customer.id}">
                                    <input type="submit" value="Delete" class="button">
                                </form>
                            </td>
                        `;
                        tbody.appendChild(tr);
                    });
                })
                .catch(error => {
                    console.error('Fetch error:', error);
                    showMessage('Failed to load customers. Check console for details.');
                })
                .finally(() => loading.style.display = 'none');
        }

        function showMessage(text) {
            const existingMsg = document.querySelector('.error-message');
            if (existingMsg) existingMsg.remove();

            const msg = document.createElement('div');
            msg.className = 'error-message';
            msg.style.color = 'red';
            msg.textContent = text;
            document.querySelector('.container').prepend(msg);
        }

        // Initial load
        document.addEventListener('DOMContentLoaded', fetchCustomers);
    </script>
</body>
</html>
