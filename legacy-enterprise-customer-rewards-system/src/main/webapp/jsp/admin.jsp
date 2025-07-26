<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "Administration - Enterprise Customer Rewards System");
%>
<jsp:include page="header.jsp" />

    <div class="navigation" style="background-color: #34495e; padding: 10px;">
        <a href="../index.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Home</a>
        <a href="customers.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Customers</a>
        <a href="points.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Points</a>
        <a href="rewards.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Rewards</a>
        <a href="admin.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Admin</a>
    </div>

    <div class="content" style="background-color: white; padding: 20px; margin: 20px 0;">
        <div class="warning">
            <strong>Warning:</strong> This is a legacy 2015 implementation. Administrative functions are basic and require manual operations.
        </div>

        <div class="admin-section">
            <h3>Service Endpoints (Internal XML)</h3>
            <ul>
                <li><strong>GET</strong> /service/customers - List all customers</li>
                <li><strong>GET</strong> /service/customers?customerId=CUST001234 - Get specific customer</li>
                <li><strong>POST</strong> /service/customers - Create new customer (form data)</li>
                <li><strong>POST</strong> /service/rewards/redeem - Redeem reward (form data)</li>
            </ul>
        </div>

        <div class="admin-section">
            <h3>Manual Operations</h3>
            <h4>Add Points Transaction (Simulation)</h4>
            <form action="admin-actions.jsp" method="post">
                <input type="hidden" name="action" value="addPoints">
                <table>
                    <tr>
                        <td>Customer ID:</td>
                        <td><input type="text" name="customerId" placeholder="CUST001234" required></td>
                    </tr>
                    <tr>
                        <td>Points Amount:</td>
                        <td><input type="number" name="pointsAmount" min="1" required></td>
                    </tr>
                    <tr>
                        <td>Description:</td>
                        <td><input type="text" name="description" placeholder="Manual adjustment" required></td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value="Add Points">
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <div class="admin-section">
            <h3>System Configuration</h3>
            <h4>Points Earning Rules (Hardcoded)</h4>
            <ul>
                <li>Base Rate: 1 point per $1 spent</li>
                <li>Bronze Tier: 1.0x multiplier</li>
                <li>Silver Tier: 1.2x multiplier</li>
                <li>Gold Tier: 1.5x multiplier</li>
                <li>Points Expiration: 24 months</li>
            </ul>
        </div>


    </div>
    <jsp:include page="footer.jsp" />
</body>
</html>
