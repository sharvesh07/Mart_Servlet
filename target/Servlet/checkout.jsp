<!-- checkout.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:include page="header.jsp" />
</head>
<body>
    <div class="center-box">
        <h2>Customer Cart</h2>
        <h3>Cart Table</h3>
        <p>[Cart items will be listed here]</p>
        <h5>Totals:</h5>
        <p>[Total amount]</p>
        <h5>Wallet:</h5>
        <p>[Wallet balance]</p>
        <form action="CustomerServlet" method="post">
            <input type="submit" value="Check Out" class="button">
        </form>
    </div>
    <div class="center">
        <a href="customer.jsp" class="button">Back</a>
    </div>
</body>
</html>
