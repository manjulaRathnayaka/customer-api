<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Error - Enterprise Customer Rewards System</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .header { background-color: #d32f2f; color: white; padding: 20px; text-align: center; }
        .content { background-color: white; padding: 20px; margin: 20px 0; }
        .error-details { background-color: #ffebee; border: 1px solid #f44336; padding: 15px; margin: 20px 0; }
    </style>
<%
    request.setAttribute("pageTitle", "Error - Enterprise Customer Rewards System");
%>
<jsp:include page="jsp/header.jsp" />
</head>
    <jsp:include page="jsp/footer.jsp" />
</body>
    <div class="content" style="background-color: white; padding: 20px; margin: 20px 0;">
        <h1>Enterprise Customer Rewards System - Error</h1>
        <p>An error occurred while processing your request</p>

    <div class="content">
        <h2>Error Information</h2>

        <div class="error-details">
            <%
                Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
                String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
                String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
                Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
            %>

            <p><strong>Status Code:</strong> <%= statusCode != null ? statusCode : "Unknown" %></p>
            <p><strong>Request URI:</strong> <%= requestUri != null ? requestUri : "Unknown" %></p>
            <p><strong>Error Message:</strong> <%= errorMessage != null ? errorMessage : "No message available" %></p>

            <%
                if (exception != null) {
            %>
            <p><strong>Exception:</strong> <%= exception.getClass().getSimpleName() %></p>
            <p><strong>Exception Message:</strong> <%= exception.getMessage() != null ? exception.getMessage() : "No details available" %></p>
            <%
                }
            %>
        </div>

        <h3>Possible Causes:</h3>
        <ul>
            <li>The requested page or resource does not exist</li>
            <li>Internal server error in the application</li>
            <li>Database connection issues (Phase 2 feature)</li>
            <li>Invalid form data or parameters</li>
            <li>Session timeout or authentication issues</li>
        </ul>

        <h3>What You Can Do:</h3>
        <ul>
            <li><a href="../index.jsp">Return to Home Page</a></li>
            <li><a href="javascript:history.back()">Go Back to Previous Page</a></li>
            <li>Check the URL for typing errors</li>
            <li>Contact the system administrator if the problem persists</li>
        </ul>

        <div style="margin-top: 30px; text-align: center;">
            <a href="../index.jsp" style="background-color: #2c5aa0; color: white; padding: 10px 20px; text-decoration: none;">
                Return to Home
            </a>
        </div>
    </div>

    <div style="text-align: center; color: #666; font-size: 12px; margin-top: 30px;">
        <p>Enterprise Customer Rewards System - Legacy Implementation (2015)</p>
        <p>Error occurred at: <%= new java.util.Date() %></p>
    </div>
    <jsp:include page="jsp/footer.jsp" />
</body>
</html>
