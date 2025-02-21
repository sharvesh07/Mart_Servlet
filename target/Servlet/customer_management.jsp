<!-- customer_management.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="header.jsp" />
</head>
<body>
    <div class="center-box">
        <h2>Customer Management</h2>
        <h3>Customer Table</h3>
        <p>[Customer data will be listed here]</p>
        <h3>Add Customer</h3>
        <p>[Form to add a customer]</p>
        <h3>Remove Customer</h3>
        <p>[Form to remove a customer]</p>
        <form action="CustomerManagementServlet" method="post">
            <input type="submit" value="Save Changes" class="button">
        </form>
    </div>
    <div class="center">
        <a href="admin.jsp" class="button">Back</a>
    </div>
</body>
</html>
