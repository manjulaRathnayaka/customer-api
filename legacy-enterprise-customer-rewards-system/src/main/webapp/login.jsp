
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Login - Enterprise Customer Rewards System");
%>

<jsp:include page="jsp/header.jsp" />
<div style="margin-left: 60px; margin-top: 40px;">
    <form method="POST" action="j_security_check">
        <table>
            <tr>
                <td style="font-size: 1.2em; padding: 8px 8px 8px 0;">Username:</td>
                <td><input type="text" name="j_username" style="font-size: 1em; width: 220px; padding: 4px;" required autocomplete="username"></td>
            </tr>
            <tr>
                <td style="font-size: 1.2em; padding: 8px 8px 8px 0;">Password:</td>
                <td><input type="password" name="j_password" style="font-size: 1em; width: 220px; padding: 4px;" required autocomplete="current-password"></td>
            </tr>
            <tr>
                <td colspan="2" style="padding-top: 10px;">
                    <input type="submit" value="Login to System" style="font-size: 1em; padding: 6px 18px;">
                </td>
            </tr>
        </table>
    </form>
    <%-- Display error message if authentication failed --%>
    <% if (request.getParameter("error") != null) { %>
        <div style="color: #b00; margin-top: 12px; font-size: 1em;">Invalid username or password. Please try again.</div>
    <% } %>
    <!-- Footer Include -->
    <jsp:include page="jsp/footer.jsp" />
</div>
</body>
