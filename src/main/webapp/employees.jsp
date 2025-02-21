<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Employee Management</title>
    <!-- Link to the external CSS file -->
    <link rel="stylesheet" type="text/css" href="css/styles.css">
</head>

<body>
    <div class="center">
        <div class="container">
            <h1 class="center">Employee Management</h1>

            <h2>Employee List</h2>
            <table id="employeeTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>Password</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Employee rows will be added here dynamically -->
                </tbody>
            </table>

            <h2>Add New Employee</h2>
            <form id="addEmployeeForm">
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" required><br><br>

                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required><br><br>

                <input type="checkbox" onclick="togglePassword()"> Show Password<br><br>

                <a href="employees.jsp"><button type="submit" class="button">Add Employee</button></a>
            </form>

            <div class="center" style="margin-top:20px;">
                <a href="admin.jsp"><button class="button">Back to Admin Menu</button></a>
            </div>
        </div>
    </div>
    <script>
        // Fetch employee list via AJAX
        function fetchEmployees() {
            fetch('EmployeeServlet')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.querySelector('#employeeTable tbody');
                    tbody.innerHTML = ''; // Clear table before adding new data
                    data.forEach(employee => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
              <td>${employee.id}</td>
              <td>${employee.name}</td>
              <td>${employee.password}</td>
              <td>
                <a href="employees.jsp"><button class="button" onclick="deleteEmployee(${employee.id}, this)">Delete</button></a>
              </td>
            `;
                        tbody.appendChild(tr);
                    });
                })
                .catch(error => console.error('Error fetching employees:', error));
        }

        // Add employee via AJAX
        document.getElementById('addEmployeeForm').addEventListener('submit', function(event) {
            event.preventDefault(); // Prevent default form submission

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            if (password.length < 6) {
                alert('Password must be at least 6 characters long.');
                return;
            }

            fetch('EmployeeServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams({
                        action: 'add',
                        username: username,
                        password: password
                    })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Employee added successfully!');
                        fetchEmployees(); // Refresh employee list
                        document.getElementById('addEmployeeForm').reset();
                    } else {
                        alert('Error adding employee.');
                    }
                })
                .catch(error => console.error('Error adding employee:', error));
        });

        // Delete employee via AJAX
        function deleteEmployee(id, button) {
            if (!confirm('Are you sure you want to delete this employee?')) return;

            fetch('EmployeeServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams({
                        action: 'delete',
                        id: id
                    })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('Employee deleted successfully!');
                        button.closest('tr').remove(); // Remove row from table
                    } else {
                        alert('Error deleting employee.');
                    }
                })
                .catch(error => console.error('Error deleting employee:', error));
        }

        // Toggle password visibility
        function togglePassword() {
            const passwordField = document.getElementById('password');
            passwordField.type = passwordField.type === 'password' ? 'text' : 'password';
        }

        // Fetch employees when page loads
        document.addEventListener('DOMContentLoaded', fetchEmployees);
    </script>
</body>

</html>