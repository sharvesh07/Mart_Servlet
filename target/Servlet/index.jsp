<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Mart Home</title>
    <style>
        .center {
            text-align: center;
            margin-top: 150px;
        }
        .button {
            padding: 15px 30px;
            font-size: 18px;
            margin: 10px;
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
    <div class="center">
        <h1>Welcome to Mart</h1>
        <a href="login.jsp"><button class="button">Login</button></a>
        <a href="register.jsp"><button class="button">Register</button></a>
        <a href="inventory.jsp"><button class="button">Inventory</button></a>
        <a href="admin.jsp"><button class="button">Admin Menu</button></a>
        <a href="customer.jsp"><button class="button">Customer Menu</button></a>
        <a href="employee.jsp"><button class="button">Employee Menu</button></a>
    </div>
</body>
</html>
