<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Enterprise Customer Rewards System" %></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f5f5f5;
            background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="2" fill="%23e0e0e0" opacity="0.3"/></svg>');
        }
        .company-logo {
            text-align: center;
            margin-bottom: 20px;
            font-size: 18px;
            color: #2c5aa0;
            font-weight: bold;
        }
        .header {
            text-align: center;
            margin-bottom: 30px;
            color: #2c5aa0;
        }
    </style>
</head>
<body>

    <div style="background: #e0e0e0; padding: 6px 18px 6px 0; text-align: right; font-size: 14px; color: #333; border-bottom: 1px solid #bdbdbd;">
        <% if (request.getRemoteUser() != null) { %>
            Welcome, <strong><%= request.getRemoteUser() %></strong>
            | <a href="<%= request.getContextPath() %>/logout.jsp" style="color: #2c5aa0; text-decoration: underline; margin-left: 10px;">Logout</a>
        <% } %>
    </div>
    <div class="header">
        <h2>🏆 Enterprise Customer Rewards System</h2>
    </div>
