<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Login Failed - Enterprise Customer Rewards System");
%>
<jsp:include page="jsp/header.jsp" />
    <div class="error-container" style="max-width: 500px; margin: 100px auto; background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center;">
        <div class="error-icon" style="font-size: 48px; color: #f44336; margin-bottom: 20px;">🔒</div>
        <div class="header" style="color: #d32f2f; margin-bottom: 30px;">
            <h2>Authentication Failed</h2>
        </div>
        <p>The username or password you entered is incorrect.</p>
        <p>Please check your credentials and try again.</p>
        <a href="login.jsp" class="retry-button" style="padding: 12px 24px; background-color: #2c5aa0; color: white; text-decoration: none; border-radius: 4px; display: inline-block; margin-top: 20px; transition: background-color 0.3s;">Try Again</a>
        <div class="system-info" style="margin-top: 30px; padding: 15px; background-color: #f8f9fa; border-radius: 4px; font-size: 12px; color: #666; text-align: left;">
            <strong>Valid User Roles:</strong><br>
            • <strong>admin</strong> - Full system access<br>
            • <strong>manager</strong> - Customer and rewards management<br>
            • <strong>employee</strong> - View-only access<br>
            <br>
            <strong>Security Features:</strong><br>
            • Session timeout: 30 minutes<br>
            <!-- Failed login tracking info removed for production UI --><br>
            <!-- Role-based authorization info removed for production UI --><br>
            <br>
            <em>If you continue to have issues, contact your system administrator.</em>
        </div>
    </div>
    <jsp:include page="jsp/footer.jsp" />
</body>
