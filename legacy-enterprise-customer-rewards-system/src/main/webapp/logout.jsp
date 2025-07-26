<%@ page language="java" session="true" %>
<%
    // Invalidate session and log out user
    if (session != null) {
        session.invalidate();
    }
    // Optionally, clear authentication (for Servlet 3.0+)
    try {
        request.logout();
    } catch (Exception e) {
        // Ignore if not supported
    }
    // Redirect to login page
    response.sendRedirect(request.getContextPath() + "/");
%>
