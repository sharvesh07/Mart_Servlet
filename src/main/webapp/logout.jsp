<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Invalidate the session if it exists
    if (session != null) {
        session.invalidate();
    }
%>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Logout</title>
    <link rel="stylesheet" type="text/css" href="css/styles.css">

    <!-- JavaScript for countdown before redirect -->
    <script>
        let countdown = 3;
        function updateCountdown() {
            if (countdown > 0) {
                document.getElementById("countdown").innerText = countdown;
                countdown--;
                setTimeout(updateCountdown, 1000);
            } else {
                window.location.href = "login.jsp";
            }
        }
        window.onload = updateCountdown;
    </script>
</head>

<body>
    <div class="message" role="alert">
        <p>You have been logged out successfully. Redirecting to the login page in <span id="countdown">3</span> seconds...</p>
        <a href="login.jsp" class="button">Go to Login</a>
    </div>
</body>

</html>