<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Employee Management</title>
  <style>
    body {
      font-family: Arial, sans-serif;
    }
    .container {
      width: 80%;
      margin: auto;
    }
    .center {
      text-align: center;
    }
    .button {
      padding: 8px 16px;
      font-size: 14px;
      background: #8000ff;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }
    .button:hover {
      background: #00c864;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
    }
    th, td {
      border: 1px solid #ddd;
      padding: 8px;
      text-align: center;
    }
    form {
      margin-top: 20px;
    }
    input[type="text"], input[type="password"] {
      padding: 8px;
      width: 200px;
      margin: 5px;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1 class="center">Employee Management</h1>

    <%-- Display status message if one is provided (e.g., after add or delete) --%>
    <%
      String status = request.getParameter("status");
      if (status != null) {
    %>
      <p class="center" style="color: green;">Status: <%= status %></p>
    <%
      }
    %>

    <h2>Employee List</h2>
    <table id="employeeTable">
      <thead>
        <tr>
          <th>ID</th>
          <th>Username</th>
          <th>Role</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
        <%-- Employee rows will be added here via JavaScript --%>
      </tbody>
    </table>

    <h2>Add New Employee</h2>
    <form action="EmployeeServlet" method="post">
      <input type="hidden" name="action" value="add">
      <label for="username">Username:</label>
      <input type="text" id="username" name="username" required>
      <label for="password">Password:</label>
      <input type="password" id="password" name="password" required>
      <input type="submit" value="Add Employee" class="button">
    </form>

    <div class="center" style="margin-top:20px;">
      <a href="admin.jsp"><button class="button">Back to Admin Menu</button></a>
    </div>
  </div>

  <script>
    // Function to fetch the list of employees using a GET request to EmployeeServlet.
    function fetchEmployees() {
      fetch('EmployeeServlet')
        .then(response => response.json())
        .then(data => {
          const tbody = document.querySelector('#employeeTable tbody');
          tbody.innerHTML = ''; // Clear any existing rows.
          data.forEach(employee => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
              <td>${employee.id}</td>
              <td>${employee.name}</td>
              <td>${employee.role}</td>
              <td>
                <form action="EmployeeServlet" method="post" onsubmit="return confirm('Are you sure you want to delete this employee?');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="id" value="${employee.id}">
                  <input type="submit" value="Delete" class="button">
                </form>
              </td>
            `;
            tbody.appendChild(tr);
          });
        })
        .catch(error => {
          console.error('Error fetching employees:', error);
        });
    }

    // Fetch employee list on page load.
    document.addEventListener('DOMContentLoaded', fetchEmployees);
  </script>
</body>
</html>
