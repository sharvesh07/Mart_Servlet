<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #6e7bff, #ff6e7b);
            margin: 0;
            padding: 0;
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .container {
            background: #fff;
            padding: 40px 50px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            text-align: center;
            max-width: 400px;
            width: 100%;
        }

        h1 {
            font-size: 28px;
            margin-bottom: 20px;
            color: #333;
        }

        .button {
            padding: 15px 30px;
            font-size: 18px;
            margin: 10px;
            background: #8000ff;
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: background 0.3s ease;
            width: 100%;
        }

        .button:hover {
            background: #00c864;
        }

        .button:focus {
            outline: none;
        }

        .error-message {
            color: #f44336;
            font-weight: bold;
            margin-bottom: 10px;
        }

        input[type="text"],
        input[type="password"] {
            width: 90%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s ease;
        }

        input[type="text"]:focus,
        input[type="password"]:focus {
            border-color: #8000ff;
            outline: none;
        }

        label {
            font-size: 14px;
            color: #333;
            text-align: left;
            display: block;
            margin-bottom: 5px;
        }

        /* Mobile responsiveness */
        @media (max-width: 500px) {
            .container {
                padding: 30px;
            }

            h1 {
                font-size: 24px;
            }

            .button {
                font-size: 16px;
                padding: 12px 20px;
            }
        }
    </style>
</head>

<body>

    <div class="container">
        <h1>Login Page</h1>

        <%-- Display error message if there's a login failure --%>
        <%
            String errorMessage = request.getParameter("error");
            if (errorMessage != null) {
        %>
        <p class="error-message"><%= errorMessage %></p>
        <%
            }
        %>

        <form action="UserServlet" method="post">
            <input type="hidden" name="action" value="login">

            <label for="userInput">User ID or Username:</label>
            <input type="text" id="userInput" name="userInput" placeholder="Enter your ID or Username" required>

            <label for="password">Password:</label>
            <input type="password" id="password" name="password" placeholder="Enter your password" required>

            <input type="checkbox" onclick="togglePassword()"> Show Password

            <br><br>
            <input type="submit" value="Login" class="button">
        </form>

        <br>
        <a href="register.jsp">
            <button class="button">Register</button>
        </a>
    </div>

    <script>
        // Toggle password visibility
        function togglePassword() {
            var passwordField = document.getElementById('password');
            var type = passwordField.type === "password" ? "text" : "password";
            passwordField.type = type;
        }
    </script>

</body>

</html>