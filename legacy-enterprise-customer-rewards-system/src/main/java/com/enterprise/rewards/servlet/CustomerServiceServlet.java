package com.enterprise.rewards.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enterprise.rewards.model.Customer;
import com.enterprise.rewards.service.DataServiceFactory;
import com.enterprise.rewards.service.DataServiceInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Customer management servlet for XML service endpoints
 * Modernized implementation - simplified for in-container compilation
 */
public class CustomerServiceServlet extends HttpServlet {
    private DataServiceInterface dataService;

    @Override
    public void init() throws ServletException {
        dataService = DataServiceFactory.getInstance(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String customerId = request.getParameter("customerId");
            if ((customerId == null || customerId.trim().isEmpty()) && request.getPathInfo() != null) {
                String pathInfo = request.getPathInfo();
                if (pathInfo != null && pathInfo.length() > 1) {
                    customerId = pathInfo.substring(1);
                }
            }

            if (customerId != null && !customerId.trim().isEmpty()) {
                Customer customer = dataService.getCustomer(customerId);
                if (customer != null) {
                    out.println(generateCustomerXml(customer));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    out.println("<error>Customer not found: " + customerId + "</error>");
                }
            } else {
                Collection<Customer> customers = dataService.getAllCustomers();
                out.println(generateCustomersListXml(customers));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<error>Internal server error: " + e.getMessage() + "</error>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String contentType = request.getContentType();
            if (contentType == null || !contentType.toLowerCase().contains("application/xml")) {
                response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                writeXmlError(out, "Only application/xml input is supported");
                return;
            }
            // Parse XML input
            javax.xml.parsers.DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(request.getInputStream());
            doc.getDocumentElement().normalize();
            org.w3c.dom.Element root = doc.getDocumentElement();
            String firstName = getXmlTagValue(root, "firstName");
            String lastName = getXmlTagValue(root, "lastName");
            String emailAddress = getXmlTagValue(root, "emailAddress");
            String phone = getXmlTagValue(root, "phoneNumber");
            if (firstName == null || lastName == null || emailAddress == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeXmlError(out, "Missing required fields: firstName, lastName, emailAddress");
                return;
            }
            Customer newCustomer = dataService.createCustomer(firstName, lastName, emailAddress, phone);
            out.println(generateCustomerXml(newCustomer));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeXmlError(out, "Error creating customer: " + e.getMessage());
        }
    }

    // XML serialization for a single Customer
    private String generateCustomerXml(Customer c) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<customer>\n");
        xml.append("  <customerId>").append(c.getCustomerId()).append("</customerId>\n");
        xml.append("  <firstName>").append(c.getFirstName()).append("</firstName>\n");
        xml.append("  <lastName>").append(c.getLastName()).append("</lastName>\n");
        xml.append("  <emailAddress>").append(c.getEmailAddress()).append("</emailAddress>\n");
        xml.append("  <phoneNumber>").append(c.getPhoneNumber() != null ? c.getPhoneNumber() : "").append("</phoneNumber>\n");
        xml.append("  <registrationDate>").append(c.getRegistrationDate()).append("</registrationDate>\n");
        xml.append("  <loyaltyTier>").append(c.getLoyaltyTier()).append("</loyaltyTier>\n");
        xml.append("  <totalLifetimePoints>").append(c.getTotalLifetimePoints()).append("</totalLifetimePoints>\n");
        xml.append("  <currentAvailablePoints>").append(c.getCurrentAvailablePoints()).append("</currentAvailablePoints>\n");
        xml.append("  <accountStatus>").append(c.getAccountStatus()).append("</accountStatus>\n");
        xml.append("</customer>");
        return xml.toString();
    }

    // XML serialization for a list of Customers
    private String generateCustomersListXml(Collection<Customer> customers) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<customers>\n");
        xml.append("  <count>").append(customers.size()).append("</count>\n");
        for (Customer c : customers) {
            xml.append("  <customer>\n");
            xml.append("    <customerId>").append(c.getCustomerId()).append("</customerId>\n");
            xml.append("    <firstName>").append(c.getFirstName()).append("</firstName>\n");
            xml.append("    <lastName>").append(c.getLastName()).append("</lastName>\n");
            xml.append("    <emailAddress>").append(c.getEmailAddress()).append("</emailAddress>\n");
            xml.append("    <phoneNumber>").append(c.getPhoneNumber() != null ? c.getPhoneNumber() : "").append("</phoneNumber>\n");
            xml.append("    <registrationDate>").append(c.getRegistrationDate()).append("</registrationDate>\n");
            xml.append("    <loyaltyTier>").append(c.getLoyaltyTier()).append("</loyaltyTier>\n");
            xml.append("    <totalLifetimePoints>").append(c.getTotalLifetimePoints()).append("</totalLifetimePoints>\n");
            xml.append("    <currentAvailablePoints>").append(c.getCurrentAvailablePoints()).append("</currentAvailablePoints>\n");
            xml.append("    <accountStatus>").append(c.getAccountStatus()).append("</accountStatus>\n");
            xml.append("  </customer>\n");
        }
        xml.append("</customers>");
        return xml.toString();
    }

    // Helper to extract XML tag value
    private String getXmlTagValue(org.w3c.dom.Element element, String tagName) {
        org.w3c.dom.NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            org.w3c.dom.Node node = nodeList.item(0);
            if (node != null && node.getTextContent() != null) {
                return node.getTextContent();
            }
        }
        return null;
    }

    // Helper to write XML error
    private void writeXmlError(PrintWriter out, String message) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<error>" + message + "</error>");
    }
}
