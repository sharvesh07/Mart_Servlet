<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.ObjectClass.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Employee Management</title>
    <style>
        body { font-family: Arial, sans-serif; }
        .container { width: 80%; margin: auto; }
        .center { text-align: center; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 10px; border: 1px solid #ddd; text-align: center; }
        .button {
            padding: 10px 20px;
            font-size: 14px;
            background: #8000ff;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        .button:hover { background: #00c864; }
        form { margin: 20px 0; }
        input[type="text"], input[type="password"] { width: 80%; padding: 8px; margin: 5px 0; }
        label { display: block; margin: 5px 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1 class="center">Employee Management</h1>

        <!-- Employee Table -->
        <h2>Current Employees</h2>
        <table>
            <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Role</th>
                <th>Action</th>
            </tr>
            <%
                List<User> employees = (List<User>) request.getAttribute("employees");
                if (employees != null && !employees.isEmpty()) {
                    for (User emp : employees) {
            %>
            <tr>
                <td><%= emp.getId() %></td>
                <td><%= emp.getName() %></td>
                <td><%= emp.getRole() %></td>
                <td>
                    <form action="EmployeeServlet" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="<%= emp.getId() %>">
                        <input type="submit" value="Delete" class="button">
                    </form>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="4">No employees found.</td>
            </tr>
            <%
                }
            %>
        </table>

        <!-- Add Employee Form -->
        <h2>Add New Employee</h2>
        <form action="EmployeeServlet" method="post">
            <input type="hidden" name="action" value="add">
            <label for="username">Username:</label>
            <input type="text" id="username" name="username" required>

            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>

            <br>
            <input type="submit" value="Add Employee" class="button">
        </form>

        <div class="center">
            <a href="index.jsp"><button class="button">Back to Home</button></a>
        </div>
    </div>
</body>
</html>
