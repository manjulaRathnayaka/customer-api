<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.enterprise.rewards.service.DataServiceInterface" %>
<%@ page import="com.enterprise.rewards.service.DataServiceFactory" %>
<%@ page import="com.enterprise.rewards.model.Customer" %>
<%@ page import="java.util.Collection" %>
<%
    request.setAttribute("pageTitle", "Customer Management - Enterprise Customer Rewards System");
%>
<jsp:include page="header.jsp" />

    <div class="navigation" style="background-color: #34495e; padding: 10px;">
        <a href="../index.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Home</a>
        <a href="customers.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Customers</a>
        <a href="points.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Points</a>
        <a href="rewards.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Rewards</a>
        <a href="admin.jsp" style="color: white; text-decoration: none; margin-right: 20px;">Admin</a>
    </div>

    <div class="content" style="background-color: white; padding: 20px; margin: 20px 0;">
        <h2>Customer List</h2>

        <%
            DataServiceInterface dataService = DataServiceFactory.getInstance(application);
            Collection<Customer> customers = dataService.getAllCustomers();
        %>

        <table>
            <thead>
                <tr>
                    <th>Customer ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Loyalty Tier</th>
                    <th>Lifetime Points</th>
                    <th>Available Points</th>
                    <th>Status</th>
                    <th>Registration Date</th>
                </tr>
            </thead>
            <tbody>
                <%
                    if (customers != null && !customers.isEmpty()) {
                        for (Customer customer : customers) {
                %>
                <tr>
                    <td><%= customer.getCustomerId() %></td>
                    <td><%= customer.getFirstName() %> <%= customer.getLastName() %></td>
                    <td><%= customer.getEmailAddress() %></td>
                    <td><%= customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "N/A" %></td>
                    <td><%= customer.getLoyaltyTier() %></td>
                    <td><%= customer.getTotalLifetimePoints() %></td>
                    <td><%= customer.getCurrentAvailablePoints() %></td>
                    <td><%= customer.getAccountStatus() %></td>
                    <td><%= customer.getRegistrationDate() %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="9">No customers found. Sample data should load automatically.</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>

        <div class="form-section">
            <h3>Add New Customer</h3>
            <form id="addCustomerForm">
                <table>
                    <tr>
                        <td>First Name:</td>
                        <td><input type="text" name="firstName" required></td>
                    </tr>
                    <tr>
                        <td>Last Name:</td>
                        <td><input type="text" name="lastName" required></td>
                    </tr>
                    <tr>
                        <td>Email Address:</td>
                        <td><input type="email" name="emailAddress" required></td>
                    </tr>
                    <tr>
                        <td>Phone Number:</td>
                        <td><input type="text" name="phoneNumber"></td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value="Add Customer">
                            <input type="reset" value="Clear">
                        </td>
                    </tr>
                </table>
            </form>
            <div id="addCustomerResult" style="margin-top:10px;"></div>
        </div>

        <div class="form-section">
            <h3>Search Customer</h3>
            <form id="searchCustomerForm">
                <table>
                    <tr>
                        <td>Customer ID:</td>
                        <td><input type="text" name="customerId" placeholder="e.g., CUST001234"></td>
                        <td><input type="submit" value="Search"></td>
                    </tr>
                </table>
            </form>
            <div id="searchCustomerResult" style="margin-top:10px;"></div>
        </div>

        <p><strong>Total Customers:</strong> <%= customers != null ? customers.size() : 0 %></p>
    </div>
    <jsp:include page="footer.jsp" />
<script type="text/javascript">
// Classic 2015-style AJAX XML form submission
document.getElementById('addCustomerForm').onsubmit = function(e) {
    e.preventDefault();
    var form = this;
    var firstName = form.firstName.value;
    var lastName = form.lastName.value;
    var emailAddress = form.emailAddress.value;
    var phoneNumber = form.phoneNumber.value;
    var xml = '<?xml version="1.0" encoding="UTF-8"?>' +
        '<customer>' +
        '<firstName>' + escapeXml(firstName) + '</firstName>' +
        '<lastName>' + escapeXml(lastName) + '</lastName>' +
        '<emailAddress>' + escapeXml(emailAddress) + '</emailAddress>' +
        '<phoneNumber>' + escapeXml(phoneNumber) + '</phoneNumber>' +
        '</customer>';

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '../service/customers', true);
    xhr.setRequestHeader('Content-Type', 'application/xml');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            var resultDiv = document.getElementById('addCustomerResult');
            if (xhr.status === 200) {
                resultDiv.style.color = 'green';
                resultDiv.innerHTML = 'Customer added successfully!';
                form.reset();
                // Optionally, reload the page or update the customer list dynamically
                setTimeout(function(){ location.reload(); }, 1000);
            } else {
                resultDiv.style.color = 'red';
                resultDiv.innerHTML = 'Error: ' + xhr.responseText;
            }
        }
    };
    xhr.send(xml);
    return false;
};

// Classic 2015-style AJAX XML search for customer by ID
document.getElementById('searchCustomerForm').onsubmit = function(e) {
    e.preventDefault();
    var form = this;
    var customerId = form.customerId.value;
    if (!customerId) {
        document.getElementById('searchCustomerResult').style.color = 'red';
        document.getElementById('searchCustomerResult').innerHTML = 'Please enter a Customer ID.';
        return false;
    }
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '../service/customers/' + encodeURIComponent(customerId), true);
    xhr.setRequestHeader('Accept', 'application/xml');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            var resultDiv = document.getElementById('searchCustomerResult');
            if (xhr.status === 200) {
                resultDiv.style.color = 'black';
                // Display raw XML or parse and show nicely
                var xml = xhr.responseXML;
                if (xml) {
                    var customer = xml.getElementsByTagName('customer')[0];
                    if (customer) {
                        var id = getXmlText(customer, 'customerId');
                        var firstName = getXmlText(customer, 'firstName');
                        var lastName = getXmlText(customer, 'lastName');
                        var email = getXmlText(customer, 'emailAddress');
                        var phone = getXmlText(customer, 'phoneNumber');
                        var tier = getXmlText(customer, 'loyaltyTier');
                        var points = getXmlText(customer, 'totalLifetimePoints');
                        var available = getXmlText(customer, 'currentAvailablePoints');
                        var status = getXmlText(customer, 'accountStatus');
                        var regDate = getXmlText(customer, 'registrationDate');
                        resultDiv.innerHTML =
                            '<b>Customer ID:</b> ' + id + '<br>' +
                            '<b>Name:</b> ' + firstName + ' ' + lastName + '<br>' +
                            '<b>Email:</b> ' + email + '<br>' +
                            '<b>Phone:</b> ' + phone + '<br>' +
                            '<b>Loyalty Tier:</b> ' + tier + '<br>' +
                            '<b>Lifetime Points:</b> ' + points + '<br>' +
                            '<b>Available Points:</b> ' + available + '<br>' +
                            '<b>Status:</b> ' + status + '<br>' +
                            '<b>Registration Date:</b> ' + regDate;
                    } else {
                        resultDiv.innerHTML = 'No customer found.';
                    }
                } else {
                    resultDiv.innerHTML = xhr.responseText;
                }
            } else {
                resultDiv.style.color = 'red';
                resultDiv.innerHTML = 'Error: ' + xhr.responseText;
            }
        }
    };
    xhr.send();
    return false;
};

function getXmlText(parent, tag) {
    var el = parent.getElementsByTagName(tag)[0];
    return el && el.textContent ? el.textContent : '';
}

function escapeXml(unsafe) {
    if (!unsafe) return '';
    return unsafe.replace(/[<>&'\"]/g, function (c) {
        switch (c) {
            case '<': return '&lt;';
            case '>': return '&gt;';
            case '&': return '&amp;';
            case '"': return '&quot;';
            case "'": return '&apos;';
        }
    });
}
</script>
</body>
</html>
