<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "Enterprise Customer Rewards System");
%>
<jsp:include page="jsp/header.jsp" />
    <div class="navigation" style="background-color: #34495e; padding: 10px;">
        <a href="index.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Home</a>
        <a href="jsp/customers.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Customers</a>
        <a href="jsp/points.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Points</a>
        <a href="jsp/rewards.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Rewards</a>
        <a href="jsp/admin.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Admin</a>
    </div>
    <div class="content" style="background-color: white; padding: 20px; margin: 20px 0;">
        <h2>Welcome to the Enterprise Customer Rewards System</h2>
        <h3>Quick Links:</h3>
        <p>
            <a href="jsp/customers.jsp">View All Customers</a> |
            <a href="jsp/points.jsp">Points Transactions</a> |
            <a href="jsp/rewards.jsp">Rewards Catalog</a>
        </p>
    </div>

    <jsp:include page="jsp/footer.jsp" />
</body>
</html>
