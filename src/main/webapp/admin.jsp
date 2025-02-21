<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.ObjectClass.User" %>
<%
    User user = (User ) session.getAttribute("user");
    if (user == null || !"manager".equalsIgnoreCase(user.getAccess())) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="css/admin.css">
</head>
<body>
    <div class="container">
        <h1>Admin Dashboard</h1>
        <nav class="navbar">
            <div class="logo">Admin Panel</div>
            <div class="nav-links">
                <span>Welcome, <%= user.getName() %></span>
                <a href="logout.jsp" class="logout-btn">Logout</a>
            </div>
        </nav>
        <div class="menu-buttons">
            <a href="inventory.jsp"><button class="button">Inventory Management</button></a>
            <a href="employees.jsp"><button class="button">Employee Management</button></a>
            <a href="customer_management.jsp"><button class="button">Customer Management</button></a>
        </div>
    </div>
</body>
</html>