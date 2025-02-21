<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.ObjectClass.User" %>
<%
    // Get the session and check if a user is logged in
    HttpSession userSession = request.getSession(false); // false prevents creating a new session if none exists
    User user = (userSession != null) ? (User) userSession.getAttribute("user") : null;

    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f0f0f0;
            text-align: center;
        }
        .container {
            width: 60%;
            margin: 100px auto;
            padding: 20px;
            background: #ffffff;
            border: 2px solid #008080;
            border-radius: 10px;
            box-shadow: 0px 0px 10px rgba(0,0,0,0.1);
        }
        .button {
            padding: 10px 20px;
            font-size: 16px;
            background: #8000ff;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        .button:hover {
            background: #00c864;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Welcome, <%= user.getName() %>!</h1>
        <p>You have successfully logged in.</p>
        <a href="index.jsp"><button class="button">Home</button></a>
        <a href="logout.jsp"><button class="button">Logout</button></a>
    </div>
</body>
</html>
