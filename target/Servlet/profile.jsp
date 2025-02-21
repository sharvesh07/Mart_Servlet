<%@ page import="com.ObjectClass.User" %>
<%
    // Ensure the user is logged in; if not, redirect to login.jsp.
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Your Profile</title>
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
        .button:hover { background: #00c864; }
        input[type="text"], input[type="password"] {
            width: 90%;
            padding: 10px;
            margin: 10px 0;
            border: 1px solid #ccc;
            border-radius: 5px;
        }
        label { font-family: Roboto; }
    </style>
</head>
<body>
    <div class="center-box">
        <h2>Your Profile</h2>
        <%
           // Display a status message if provided (e.g., ?status=updated)
           String status = request.getParameter("status");
           if (status != null) {
        %>
            <p style="color: green;"><%= status %></p>
        <% } %>
        <form action="CustomerServlet" method="post">
            <input type="hidden" name="action" value="editProfile">
            <label for="name">Name:</label><br>
            <input type="text" id="name" name="name" value="<%= user.getName() %>" required><br>
            <label for="password">New Password:</label><br>
            <input type="password" id="password" name="password" placeholder="Enter new password"><br><br>
            <input type="submit" value="Update Profile" class="button">
        </form>
        <br>
        <a href="index.jsp"><button class="button">Back to Home</button></a>
    </div>
</body>
</html>
