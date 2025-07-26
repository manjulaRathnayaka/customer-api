<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.enterprise.rewards.service.DataServiceInterface" %>
<%@ page import="com.enterprise.rewards.service.DataServiceFactory" %>
<%@ page import="com.enterprise.rewards.model.Reward" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    request.setAttribute("pageTitle", "Rewards Catalog - Enterprise Customer Rewards System");
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
    <h3>Add New Reward (Admin Only)</h3>
    <form action="../service/rewards" method="post">
        <table>
            <tr>
                <td>Reward ID:</td>
                <td><input type="text" name="rewardId" required></td>
            </tr>
            <tr>
                <td>Name:</td>
                <td><input type="text" name="rewardName" required></td>
            </tr>
            <tr>
                <td>Points Required:</td>
                <td><input type="number" name="pointsRequired" required></td>
            </tr>
            <tr>
                <td>Type:</td>
                <td><input type="text" name="rewardType" required></td>
            </tr>
            <tr>
                <td>Value:</td>
                <td><input type="text" name="rewardValue"></td>
            </tr>
            <tr>
                <td>Available Count:</td>
                <td><input type="number" name="availabilityCount" value="100"></td>
            </tr>
            <tr>
                <td>Category:</td>
                <td><input type="text" name="category"></td>
            </tr>
            <tr>
                <td>Description:</td>
                <td><input type="text" name="description"></td>
            </tr>
            <tr>
                <td>Is Active:</td>
                <td>
                    <select name="isActive">
                        <option value="true" selected>Active</option>
                        <option value="false">Inactive</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="submit" value="Add Reward">
                    <input type="reset" value="Clear">
                </td>
            </tr>
        </table>
    </form>


    <div class="content" style="background-color: white; padding: 20px; margin: 20px 0;">
        <h2>Available Rewards</h2>

        <%
            DataServiceInterface dataService = DataServiceFactory.getInstance(application);
            Collection<Reward> activeRewards = dataService.getAllRewards();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        %>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px;">
            <%
                if (activeRewards != null && !activeRewards.isEmpty()) {
                    for (Reward reward : activeRewards) {
            %>
            <div class="reward-card">
                <h3><%= reward.getRewardName() %></h3>
                <p class="reward-points"><%= reward.getPointsRequired() %> Points Required</p>
                <p><strong>Type:</strong> <%= reward.getRewardType() %></p>
                <p><strong>Value:</strong> <%= reward.getRewardValue() %></p>
                <p><strong>Category:</strong> <%= reward.getCategory() %></p>
                <p><strong>Available:</strong> <%= reward.getAvailabilityCount() %> remaining</p>
                <%
                    if (reward.getExpirationDate() != null) {
                %>
                <p><strong>Expires:</strong> <%= dateFormat.format(reward.getExpirationDate()) %></p>
                <%
                    }
                %>
                <form action="../service/rewards/redeem" method="post" style="margin-top: 10px;">
                    <input type="hidden" name="rewardId" value="<%= reward.getRewardId() %>">
                    <input type="text" name="customerId" placeholder="Customer ID" required>
                    <input type="submit" value="Redeem" style="background-color: #2c5aa0; color: white; border: none; padding: 5px 10px;">
                </form>
            </div>
            <%
                    }
                } else {
            %>
            <p>No active rewards available. Administrator should configure rewards.</p>
            <%
                }
            %>
        </div>

        <h2>All Rewards (Administrative View)</h2>

        <%
            Collection<Reward> allRewards = dataService.getAllRewards();
        %>

        <table>
            <thead>
                <tr>
                    <th>Reward ID</th>
                    <th>Name</th>
                    <th>Points Required</th>
                    <th>Type</th>
                    <th>Value</th>
                    <th>Available Count</th>
                    <th>Category</th>
                    <th>Expiration Date</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (allRewards != null && !allRewards.isEmpty()) {
                        for (Reward reward : allRewards) {
                            String statusClass = reward.isActive() ? "active" : "inactive";
                %>
                <tr>
                    <td><%= reward.getRewardId() %></td>
                    <td><%= reward.getRewardName() %></td>
                    <td><%= reward.getPointsRequired() %></td>
                    <td><%= reward.getRewardType() %></td>
                    <td><%= reward.getRewardValue() %></td>
                    <td><%= reward.getAvailabilityCount() %></td>
                    <td><%= reward.getCategory() %></td>
                    <td><%= reward.getExpirationDate() != null ? dateFormat.format(reward.getExpirationDate()) : "No expiration" %></td>
                    <td class="<%= statusClass %>"><%= reward.isActive() ? "ACTIVE" : "INACTIVE" %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="9">No rewards configured. Sample data should load automatically.</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>

        <h3>Reward Categories Available:</h3>
        <ul>
            <li><strong>PERCENTAGE_DISCOUNT:</strong> Percentage off next purchase</li>
            <li><strong>DOLLAR_DISCOUNT:</strong> Fixed dollar amount off purchase</li>
            <li><strong>SHIPPING_BENEFIT:</strong> Free or discounted shipping</li>
            <li><strong>FREE_PRODUCT:</strong> Complimentary items (future feature)</li>
        </ul>

        <p><strong>Total Rewards:</strong> <%= allRewards != null ? allRewards.size() : 0 %></p>
        <p><strong>Active Rewards:</strong> <%= activeRewards != null ? activeRewards.size() : 0 %></p>
    </div>
    <jsp:include page="footer.jsp" />
</body>
</html>
