package com.enterprise.rewards.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enterprise.rewards.model.PointsTransaction;
import com.enterprise.rewards.service.DataServiceFactory;
import com.enterprise.rewards.service.DataServiceInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TransactionServiceServlet extends HttpServlet {
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
            String txnId = request.getParameter("transactionId");
            if ((txnId == null || txnId.trim().isEmpty()) && request.getPathInfo() != null) {
                String pathInfo = request.getPathInfo();
                if (pathInfo != null && pathInfo.length() > 1) {
                    txnId = pathInfo.substring(1);
                }
            }

            if (txnId != null && !txnId.trim().isEmpty()) {
                PointsTransaction txn = dataService.getTransaction(txnId);
                if (txn != null) {
                    out.println(generateTransactionXml(txn));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    out.println("<error>Transaction not found: " + txnId + "</error>");
                }
            } else {
                List<PointsTransaction> txns = dataService.getAllTransactions();
                out.println(generateTransactionsListXml(txns));
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
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.println("<error>Only application/xml input is supported</error>");
                return;
            }
            // Read and print raw XML input for debugging
            StringBuilder rawXml = new StringBuilder();
            java.io.BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                rawXml.append(line).append("\n");
            }
            // Parse XML from string
            javax.xml.parsers.DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            java.io.ByteArrayInputStream xmlInput = new java.io.ByteArrayInputStream(rawXml.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = dBuilder.parse(xmlInput);
            doc.getDocumentElement().normalize();
            org.w3c.dom.Element root = doc.getDocumentElement();
            String customerId = getXmlTagValue(root, "customerId");
            String transactionType = getXmlTagValue(root, "transactionType");
            String pointsAmountStr = getXmlTagValue(root, "pointsAmount");
            if (pointsAmountStr == null) pointsAmountStr = getXmlTagValue(root, "amount");
            String description = getXmlTagValue(root, "description");
            if (customerId == null || transactionType == null || pointsAmountStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.println("<error>Missing required fields: customerId, transactionType, pointsAmount</error>");
                return;
            }
            int pointsAmount = Integer.parseInt(pointsAmountStr);
            PointsTransaction txn = dataService.createTransaction(customerId, transactionType, pointsAmount, description);
            out.println(generateTransactionXml(txn));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<error>Error creating transaction: " + e.getMessage() + "</error>");
        }
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

    // XML serialization for PointsTransaction
    private String generateTransactionXml(PointsTransaction txn) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<transaction>\n");
        xml.append("  <transactionId>").append(txn.getTransactionId()).append("</transactionId>\n");
        xml.append("  <customerId>").append(txn.getCustomerId()).append("</customerId>\n");
        xml.append("  <transactionType>").append(txn.getTransactionType()).append("</transactionType>\n");
        xml.append("  <pointsAmount>").append(txn.getPointsAmount()).append("</pointsAmount>\n");
        xml.append("  <transactionDate>").append(txn.getTransactionDate()).append("</transactionDate>\n");
        xml.append("  <expirationDate>").append(txn.getExpirationDate()).append("</expirationDate>\n");
        xml.append("  <relatedOrderId>").append(txn.getRelatedOrderId() != null ? txn.getRelatedOrderId() : "").append("</relatedOrderId>\n");
        xml.append("  <description>").append(txn.getDescription() != null ? txn.getDescription() : "").append("</description>\n");
        xml.append("  <createdBy>").append(txn.getCreatedBy() != null ? txn.getCreatedBy() : "").append("</createdBy>\n");
        xml.append("  <status>").append(txn.getStatus() != null ? txn.getStatus() : "").append("</status>\n");
        xml.append("</transaction>");
        return xml.toString();
    }

    // XML serialization for a list of PointsTransaction
    private String generateTransactionsListXml(List<PointsTransaction> txns) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<transactions>\n");
        xml.append("  <count>").append(txns.size()).append("</count>\n");
        for (PointsTransaction txn : txns) {
            xml.append("  <transaction>\n");
            xml.append("    <transactionId>").append(txn.getTransactionId()).append("</transactionId>\n");
            xml.append("    <customerId>").append(txn.getCustomerId()).append("</customerId>\n");
            xml.append("    <transactionType>").append(txn.getTransactionType()).append("</transactionType>\n");
            xml.append("    <pointsAmount>").append(txn.getPointsAmount()).append("</pointsAmount>\n");
            xml.append("    <transactionDate>").append(txn.getTransactionDate()).append("</transactionDate>\n");
            xml.append("    <expirationDate>").append(txn.getExpirationDate()).append("</expirationDate>\n");
            xml.append("    <relatedOrderId>").append(txn.getRelatedOrderId() != null ? txn.getRelatedOrderId() : "").append("</relatedOrderId>\n");
            xml.append("    <description>").append(txn.getDescription() != null ? txn.getDescription() : "").append("</description>\n");
            xml.append("    <createdBy>").append(txn.getCreatedBy() != null ? txn.getCreatedBy() : "").append("</createdBy>\n");
            xml.append("    <status>").append(txn.getStatus() != null ? txn.getStatus() : "").append("</status>\n");
            xml.append("  </transaction>\n");
        }
        xml.append("</transactions>");
        return xml.toString();
    }
}
