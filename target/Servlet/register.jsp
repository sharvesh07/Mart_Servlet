<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Register</title>
    <style>
        .center-box {
            text-align: center;
            width: 40%;
            margin: 100px auto;
            padding: 20px;
            border: 2px solid #008080;
            border-radius: 10px;
            background: #f8f8f8;
            box-shadow: 0px 0px 10px rgba(0,0,0,0.1);
        }
        .button {
            padding: 10px 20px;
            font-size: 16px;
            border: none;
            border-radius: 5px;
            background: #8000ff;
            color: white;
            cursor: pointer;
        }
        .button:hover {
            background: #00c864;
        }
        input[type="text"], input[type="password"] {
            width: 90%;
            padding: 10px;
            margin: 10px 0;
            border: 1px solid #ccc;
            border-radius: 5px;
        }
        label {
            font-family: Roboto;
        }
    </style>
</head>
<body>
    <div class="center-box">
        <h2>Register Page</h2>
            <form action="UserServlet" method="post">
                <input type="hidden" name="action" value="register">
                <label for="username">Username:</label><br>
                <input type="text" id="username" name="username" placeholder="Enter your username" required><br>
                <label for="password">Password:</label><br>
                <input type="password" id="password" name="password" placeholder="Enter your password" required><br><br>
                <input type="submit" value="Register" class="button">
            </form>
        <br>
        <a href="login.jsp"><button class="button">Back to Login</button></a>
    </div>
</body>
</html>
