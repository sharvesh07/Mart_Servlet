<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mart Home</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background: linear-gradient(to right, #6e7bff, #ff6e7b);
            /* Gradient added here */
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .center {
            text-align: center;
        }

        h1 {
            font-size: 32px;
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
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
        }

        .button:hover {
            background: #00c864;
        }

        /* Mobile responsiveness */
        @media screen and (max-width: 600px) {
            .button {
                width: 100%;
                font-size: 16px;
                padding: 12px;
            }

            .center {
                margin-top: 50px;
            }
        }
    </style>
</head>

<body>
    <div class="center">
        <h1>Welcome to Mart</h1>
        <a href="login.jsp" class="button" aria-label="Go to Login Page">Login</a>
        <a href="register.jsp" class="button" aria-label="Go to Register Page">Register</a>
    </div>
</body>

</html>