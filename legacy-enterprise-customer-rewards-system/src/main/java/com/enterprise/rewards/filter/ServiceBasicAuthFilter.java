
package com.enterprise.rewards.filter;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter({"/service/*", "/customers/*"})
public class ServiceBasicAuthFilter implements Filter {

    public ServiceBasicAuthFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("ServiceBasicAuthFilter initialized - protecting /service/* endpoints");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow if user is already authenticated via session (FORM login)
        if (httpRequest.getUserPrincipal() != null) {
            chain.doFilter(request, response);
            return;
        }

        // Otherwise, require HTTP Basic Auth
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length()).trim();
                String credentials = new String(Base64.getDecoder().decode(base64Credentials), "UTF-8");
                String[] values = credentials.split(":", 2);
                if (values.length == 2) {
                    String username = values[0];
                    String password = values[1];
                    String userRole = this.validateCredentials(username, password, httpRequest);
                    if (userRole != null) {
                        chain.doFilter(request, response);
                        return;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error parsing Basic Auth: " + ex.getMessage());
            }
            this.sendAuthenticationChallenge(httpResponse);
        } else {
            this.sendAuthenticationChallenge(httpResponse);
        }
    }

    private String validateCredentials(String username, String password, HttpServletRequest request) {
        try {
            request.login(username, password);
            if (request.isUserInRole("admin")) {
                return "admin";
            }
            if (request.isUserInRole("manager")) {
                return "manager";
            }
            if (request.isUserInRole("employee")) {
                return "employee";
            }
        } catch (ServletException ex) {
            // Ignore
        }
        return null;
    }

    private void sendAuthenticationChallenge(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"Customer Loyalty Manager\"");
        response.setStatus(401);
        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        PrintWriter out = response.getWriter();
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<error>Authentication required</error>");
        out.flush();
    }

    @Override
    public void destroy() {
        System.out.println("ServiceBasicAuthFilter destroyed");
    }
}