<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.ObjectClass.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"employee".equalsIgnoreCase(user.getAccess())) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employee Dashboard</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>

    <div class="container">
        <h1>Employee Dashboard</h1>
        <nav class="navbar">
            <div class="logo">Employee Panel</div>
            <div class="nav-links">
                <span>Welcome, <%= user.getName() %></span>
                <a href="logout.jsp" class="logout-btn">Logout</a>
            </div>
        </nav>
        <div class="menu-buttons">
            <a href="emp_inventory.jsp"><button class="button">Manage Inventory</button></a>
        </div>
    </div>
</body>
</html>
