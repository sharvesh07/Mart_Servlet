<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Menu</title>
    <style>
        .center {
            text-align: center;
            margin-top: 150px;
            font-family: Arial, sans-serif;
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
        <h1>Admin Menu</h1>
        <a href="inventory.jsp"><button class="button">Inventory Management</button></a>
        <a href="employee.jsp"><button class="button">Employee Management</button></a>
        <a href="customer.jsp"><button class="button">Customer Management</button></a>
        <br>
        <a href="index.jsp"><button class="button">Back to Home</button></a>
    </div>
</body>
</html>
