<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.ObjectClass.User" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-color: #f4f4f9;
            margin: 0;
            padding: 0;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 40px 50px;
            background: #ffffff;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            text-align: center;
        }

        .button {
            padding: 15px 30px;
            font-size: 16px;
            border: none;
            border-radius: 8px;
            background: #8000ff;
            color: white;
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
            font-family: Roboto, Arial, sans-serif;
            font-size: 14px;
            color: #333;
            text-align: left;
            display: block;
            margin-bottom: 5px;
        }

        .warning {
            color: #f44336;
            font-weight: bold;
            margin-bottom: 10px;
        }

        /* Mobile responsiveness */
        @media screen and (max-width: 600px) {
            .container {
                padding: 30px;
            }

            .button {
                font-size: 16px;
                padding: 12px 20px;
            }
        }

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
    </style>
</head>

<body>

    <div class="container">
        <div class="center-box">
            <h2>Register Page</h2>

            <%-- Display any error messages passed in the request --%>
            <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null) {
            %>
            <p class="warning"><%= errorMessage %></p>
            <%
                }
            %>

            <form action="UserServlet" method="post" onsubmit="return validateForm()">
                <input type="hidden" name="action" value="register">
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" placeholder="Enter your username" required>

                <label for="password">Password:</label>
                <input type="password" id="password" name="password" placeholder="Enter your password" required>

                <input type="submit" value="Register" class="button">
            </form>

            <br>
            <a href="login.jsp">
                <button class="button">Back to Login</button>
            </a>
        </div>
    </div>

    <script>
        // Validate password length before submission
        function validateForm() {
            var password = document.getElementById("password").value;
            if (password.length < 8) {
                alert("Password must be at least 8 characters long.");
                return false;
            }
            return true;
        }
    </script>

</body>

</html>