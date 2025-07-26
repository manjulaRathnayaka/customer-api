<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.enterprise.rewards.service.DataServiceInterface" %>
<%@ page import="com.enterprise.rewards.service.DataServiceFactory" %>
<%@ page import="com.enterprise.rewards.model.PointsTransaction" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    request.setAttribute("pageTitle", "Points Transactions - Enterprise Customer Rewards System");
%>
<jsp:include page="header.jsp" />

<div class="navigation" style="background-color: #34495e; padding: 10px;">
    <a href="../index.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Home</a>
    <a href="customers.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Customers</a>
    <a href="points.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Points</a>
    <a href="rewards.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Rewards</a>
    <a href="admin.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Admin</a>
</div>

<div class="form-section">
    <h3>Add New Points Transaction</h3>
    <form id="addPointsForm">
        <table>
            <tr>
                <td>Customer ID:</td>
                <td><input type="text" name="customerId" required></td>
            </tr>
            <tr>
                <td>Type:</td>
                <td>
                    <select name="transactionType" required>
                        <option value="EARN">EARN</option>
                        <option value="REDEEM">REDEEM</option>
                        <option value="ADJUST">ADJUST</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>Points Amount:</td>
                <td><input type="number" name="pointsAmount" required></td>
            </tr>
            <tr>
                <td>Description:</td>
                <td><input type="text" name="description"></td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="submit" value="Add Transaction">
                    <input type="reset" value="Clear">
                </td>
            </tr>
        </table>
    </form>
    <div id="addPointsResult" style="margin-top:10px;"></div>
</div>

<div class="content" style="background-color: white; padding: 20px; margin: 20px 0;">
        <h2>All Points Transactions</h2>

        <%
            DataServiceInterface dataService = null;
            List<PointsTransaction> transactions = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dataServiceError = null;
            try {
                dataService = DataServiceFactory.getInstance(application);
                if (dataService != null) {
                    transactions = dataService.getAllTransactions();
                } else {
                    dataServiceError = "Data service is unavailable.";
                }
            } catch (Exception ex) {
                dataServiceError = "Error loading transactions: " + ex.getMessage();
            }
        %>

        <table>
            <thead>
                <tr>
                    <th>Transaction ID</th>
                    <th>Customer ID</th>
                    <th>Type</th>
                    <th>Points Amount</th>
                    <th>Transaction Date</th>
                    <th>Expiration Date</th>
                    <th>Related Order</th>
                    <th>Description</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (dataServiceError != null) {
                %>
                <tr>
                    <td colspan="9" style="color:red;"><%= dataServiceError %></td>
                </tr>
                <%
                    } else if (transactions != null && !transactions.isEmpty()) {
                        for (PointsTransaction transaction : transactions) {
                            String cssClass = "EARN".equals(transaction.getTransactionType()) ? "earned" : "redeemed";
                            String sign = "";
                            if ("EARN".equals(transaction.getTransactionType())) {
                                sign = "+";
                            } else if ("REDEEM".equals(transaction.getTransactionType())) {
                                sign = "-";
                            } else if ("ADJUST".equals(transaction.getTransactionType())) {
                                sign = transaction.getPointsAmount() >= 0 ? "+" : "-";
                            }
                %>
                <tr>
                    <td><%= transaction.getTransactionId() %></td>
                    <td><%= transaction.getCustomerId() %></td>
                    <td class="<%= cssClass %>"><%= transaction.getTransactionType() %></td>
                    <td class="<%= cssClass %>">
                        <%= sign %><%= Math.abs(transaction.getPointsAmount()) %>
                    </td>
                    <td><%= dateFormat.format(transaction.getTransactionDate()) %></td>
                    <td><%= transaction.getExpirationDate() != null ? dateFormat.format(transaction.getExpirationDate()) : "N/A" %></td>
                    <td><%= transaction.getRelatedOrderId() != null ? transaction.getRelatedOrderId() : "N/A" %></td>
                    <td><%= transaction.getDescription() != null ? transaction.getDescription() : "N/A" %></td>
                    <td><%= transaction.getStatus() %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="9">No transactions found. Sample data should load automatically.</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>

        <div class="form-section">
            <h3>Search Transactions by Customer</h3>
            <form action="points.jsp" method="get">
                <table>
                    <tr>
                        <td>Customer ID:</td>
                        <td><input type="text" name="customerId" placeholder="e.g., CUST001234"
                                 value="<%= request.getParameter("customerId") != null ? request.getParameter("customerId") : "" %>"></td>
                        <td><input type="submit" value="Filter"></td>
                        <td><a href="points.jsp">Show All</a></td>
                    </tr>
                </table>
            </form>
        </div>

        <%
            String filterCustomerId = request.getParameter("customerId");
            if (filterCustomerId != null && !filterCustomerId.trim().isEmpty()) {
                List<PointsTransaction> customerTransactions = dataService.getTransactionsByCustomer(filterCustomerId);
        %>
        <div class="form-section">
            <h3>Transactions for Customer: <%= filterCustomerId %></h3>
            <table>
                <thead>
                    <tr>
                        <th>Transaction ID</th>
                        <th>Type</th>
                        <th>Points Amount</th>
                        <th>Transaction Date</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (customerTransactions != null && !customerTransactions.isEmpty()) {
                            for (PointsTransaction transaction : customerTransactions) {
                                String cssClass = "EARN".equals(transaction.getTransactionType()) ? "earned" : "redeemed";
                                String sign = "";
                                if ("EARN".equals(transaction.getTransactionType())) {
                                    sign = "+";
                                } else if ("REDEEM".equals(transaction.getTransactionType())) {
                                    sign = "-";
                                } else if ("ADJUST".equals(transaction.getTransactionType())) {
                                    sign = transaction.getPointsAmount() >= 0 ? "+" : "-";
                                }
                    %>
                    <tr>
                        <td><%= transaction.getTransactionId() %></td>
                        <td class="<%= cssClass %>"><%= transaction.getTransactionType() %></td>
                        <td class="<%= cssClass %>">
                            <%= sign %><%= Math.abs(transaction.getPointsAmount()) %>
                        </td>
                        <td><%= dateFormat.format(transaction.getTransactionDate()) %></td>
                        <td><%= transaction.getDescription() != null ? transaction.getDescription() : "N/A" %></td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr>
                        <td colspan="5">No transactions found for this customer.</td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
        <%
            }
        %>

        <p><strong>Total Transactions:</strong> <%= transactions != null ? transactions.size() : 0 %></p>
    </div>
    <jsp:include page="footer.jsp" />

<script type="text/javascript">
document.getElementById('addPointsForm').onsubmit = function(e) {
    e.preventDefault();
    var form = this;
    var customerId = form.customerId.value;
    var transactionType = form.transactionType.value;
    var pointsAmount = form.pointsAmount.value;
    var description = form.description.value;
    var xml = '<?xml version="1.0" encoding="UTF-8"?>' +
        '<transaction>' +
        '<customerId>' + escapeXml(customerId) + '</customerId>' +
        '<transactionType>' + escapeXml(transactionType) + '</transactionType>' +
        '<pointsAmount>' + escapeXml(pointsAmount) + '</pointsAmount>' +
        '<description>' + escapeXml(description) + '</description>' +
        '</transaction>';

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '../service/transactions', true);
    xhr.setRequestHeader('Content-Type', 'application/xml');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            var resultDiv = document.getElementById('addPointsResult');
            if (xhr.status === 200) {
                resultDiv.style.color = 'green';
                resultDiv.innerHTML = 'Transaction added successfully!';
                form.reset();
                setTimeout(function(){ location.reload(); }, 1000);
            } else {
                resultDiv.style.color = 'red';
                resultDiv.innerHTML = 'Error: ' + xhr.responseText;
            }
        }
    };
    xhr.send(xml);
    return false;
};

function escapeXml(unsafe) {
    if (!unsafe) return '';
    return unsafe.replace(/[<>&'\"]/g, function (c) {
        switch (c) {
            case '<': return '&lt;';
            case '>': return '&gt;';
            case '&': return '&amp;';
            case '"': return '&quot;';
            case "'": return '&apos;';
        }
    });
}
</script>


</body>
</html>
