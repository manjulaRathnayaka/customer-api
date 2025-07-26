package com.enterprise.rewards.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enterprise.rewards.model.Reward;
import com.enterprise.rewards.service.DataServiceFactory;
import com.enterprise.rewards.service.DataServiceInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class RewardServiceServlet extends HttpServlet {
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
            String rewardId = request.getParameter("rewardId");
            if ((rewardId == null || rewardId.trim().isEmpty()) && request.getPathInfo() != null) {
                String pathInfo = request.getPathInfo();
                if (pathInfo != null && pathInfo.length() > 1) {
                    rewardId = pathInfo.substring(1);
                }
            }
            if (rewardId != null && !rewardId.trim().isEmpty()) {
                Reward reward = dataService.getReward(rewardId);
                if (reward != null) {
                    out.println(generateRewardXml(reward));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writeXmlError(out, "Reward not found: " + rewardId);
                }
            } else {
                Collection<Reward> rewards = dataService.getAllRewards();
                out.println(generateRewardsListXml(rewards));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeXmlError(out, "Internal server error: " + e.getMessage());
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
            javax.xml.parsers.DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(request.getInputStream());
            doc.getDocumentElement().normalize();
            org.w3c.dom.Element root = doc.getDocumentElement();
            String rewardId = getXmlTagValue(root, "rewardId");
            String rewardName = getXmlTagValue(root, "rewardName");
            String pointsRequiredStr = getXmlTagValue(root, "pointsRequired");
            String rewardType = getXmlTagValue(root, "rewardType");
            String rewardValue = getXmlTagValue(root, "rewardValue");
            String category = getXmlTagValue(root, "category");
            String description = getXmlTagValue(root, "description");
            String isActiveStr = getXmlTagValue(root, "isActive");
            String availabilityCountStr = getXmlTagValue(root, "availabilityCount");
            if (rewardId == null || rewardName == null || pointsRequiredStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeXmlError(out, "Missing required fields: rewardId, rewardName, pointsRequired");
                return;
            }
            int pointsRequired = Integer.parseInt(pointsRequiredStr);
            int availabilityCount = (availabilityCountStr != null) ? Integer.parseInt(availabilityCountStr) : 100;
            boolean isActive = (isActiveStr == null || isActiveStr.equalsIgnoreCase("true"));
            Reward reward = new Reward();
            reward.setRewardId(rewardId);
            reward.setRewardName(rewardName);
            reward.setPointsRequired(pointsRequired);
            reward.setRewardType(rewardType);
            reward.setRewardValue(rewardValue);
            reward.setCategory(category);
            reward.setDescription(description);
            reward.setActive(isActive);
            reward.setAvailabilityCount(availabilityCount);
            dataService.saveReward(reward);
            out.println(generateRewardXml(reward));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeXmlError(out, "Error creating reward: " + e.getMessage());
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

    // Helper to write XML error
    private void writeXmlError(PrintWriter out, String message) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<error>" + escapeXml(message) + "</error>");
    }

    // Escape XML special characters
    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    // XML serialization for a single Reward
    private String generateRewardXml(Reward r) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<reward>\n");
        xml.append("  <rewardId>").append(escapeXml(r.getRewardId())).append("</rewardId>\n");
        xml.append("  <rewardName>").append(escapeXml(r.getRewardName())).append("</rewardName>\n");
        xml.append("  <pointsRequired>").append(r.getPointsRequired()).append("</pointsRequired>\n");
        xml.append("  <rewardType>").append(escapeXml(r.getRewardType())).append("</rewardType>\n");
        xml.append("  <rewardValue>").append(escapeXml(r.getRewardValue())).append("</rewardValue>\n");
        xml.append("  <availabilityCount>").append(r.getAvailabilityCount()).append("</availabilityCount>\n");
        xml.append("  <category>").append(escapeXml(r.getCategory())).append("</category>\n");
        xml.append("  <description>").append(escapeXml(r.getDescription())).append("</description>\n");
        xml.append("  <isActive>").append(r.isActive()).append("</isActive>\n");
        xml.append("</reward>");
        return xml.toString();
    }

    // XML serialization for a list of Rewards
    private String generateRewardsListXml(Collection<Reward> rewards) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rewards>\n");
        xml.append("  <count>").append(rewards.size()).append("</count>\n");
        for (Reward r : rewards) {
            xml.append("  <reward>\n");
            xml.append("    <rewardId>").append(escapeXml(r.getRewardId())).append("</rewardId>\n");
            xml.append("    <rewardName>").append(escapeXml(r.getRewardName())).append("</rewardName>\n");
            xml.append("    <pointsRequired>").append(r.getPointsRequired()).append("</pointsRequired>\n");
            xml.append("    <rewardType>").append(escapeXml(r.getRewardType())).append("</rewardType>\n");
            xml.append("    <rewardValue>").append(escapeXml(r.getRewardValue())).append("</rewardValue>\n");
            xml.append("    <availabilityCount>").append(r.getAvailabilityCount()).append("</availabilityCount>\n");
            xml.append("    <category>").append(escapeXml(r.getCategory())).append("</category>\n");
            xml.append("    <description>").append(escapeXml(r.getDescription())).append("</description>\n");
            xml.append("    <isActive>").append(r.isActive()).append("</isActive>\n");
            xml.append("  </reward>\n");
        }
        xml.append("</rewards>");
        return xml.toString();
    }
}
