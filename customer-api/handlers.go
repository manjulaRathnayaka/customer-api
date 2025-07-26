package main

import (
	"encoding/xml"
	"io"
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

// =========================
// Handlers (Public API)
// =========================

// ListCustomersHandler handles GET /api/v2/customers
// Fetches all customers from backend, applies optional filters, and returns paginated results.
// NOTE: This implementation fetches all customers because the backend does not support search or pagination.
//
//	For production, a caching layer should be introduced to avoid performance issues with large datasets.
func ListCustomersHandler(c *gin.Context, cfg Config, client *http.Client, logger *log.Logger) {
	q := c.Request.URL.Query()
	limit := 25
	if l := q.Get("limit"); l != "" {
		if v, err := strconv.Atoi(l); err == nil && v > 0 {
			limit = v
		}
	}
	offset := 0
	if o := q.Get("offset"); o != "" {
		if v, err := strconv.Atoi(o); err == nil && v >= 0 {
			offset = v
		}
	}
	req, err := http.NewRequest("GET", cfg.Backend+"/customers", nil)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	req.SetBasicAuth(cfg.BackendUser, cfg.BackendPass)
	resp, err := client.Do(req)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", nil)
		return
	}
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	var xmlResp xmlCustomers
	err = xml.Unmarshal(body, &xmlResp)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	if xmlResp.Customers == nil {
		xmlResp.Customers = []xmlCustomer{}
	}

	// --- Filtering logic for search parameters ---
	firstName := strings.ToLower(q.Get("firstName"))
	lastName := strings.ToLower(q.Get("lastName"))
	emailAddress := strings.ToLower(q.Get("emailAddress"))
	phoneNumber := strings.ToLower(q.Get("phoneNumber"))
	accountStatus := strings.ToLower(q.Get("accountStatus"))

	filtered := make([]xmlCustomer, 0, len(xmlResp.Customers))
	for _, cust := range xmlResp.Customers {
		if firstName != "" && !strings.Contains(strings.ToLower(cust.FirstName), firstName) {
			continue
		}
		if lastName != "" && !strings.Contains(strings.ToLower(cust.LastName), lastName) {
			continue
		}
		if emailAddress != "" && !strings.Contains(strings.ToLower(cust.EmailAddress), emailAddress) {
			continue
		}
		if phoneNumber != "" && !strings.Contains(strings.ToLower(cust.PhoneNumber), phoneNumber) {
			continue
		}
		if accountStatus != "" && strings.ToLower(cust.AccountStatus) != accountStatus {
			continue
		}
		filtered = append(filtered, cust)
	}

	total := len(filtered)
	start := offset
	if start > total {
		start = total
	}
	end := offset + limit
	if end > total {
		end = total
	}
	paged := filtered[start:end]
	customers := make([]Customer, 0, len(paged))
	for _, x := range paged {
		customers = append(customers, xmlToCustomer(x))
	}
	c.JSON(http.StatusOK, CustomersResponse{
		Customers:  customers,
		Pagination: Pagination{Offset: offset, Limit: limit, Total: total},
	})
}

// CreateCustomerHandler handles POST /api/v2/customers
// Creates a new customer by forwarding JSON input as XML to the backend, returns created customer.
func CreateCustomerHandler(c *gin.Context, cfg Config, client *http.Client, logger *log.Logger) {
	var reqBody struct {
		FirstName    string `json:"firstName"`
		LastName     string `json:"lastName"`
		EmailAddress string `json:"emailAddress"`
		PhoneNumber  string `json:"phoneNumber"`
	}
	if err := c.ShouldBindJSON(&reqBody); err != nil {
		respondError(c, logger, http.StatusBadRequest, "Invalid request payload", "BAD_REQUEST", err)
		return
	}
	type createCustomerXML struct {
		XMLName      xml.Name `xml:"customer"`
		FirstName    string   `xml:"firstName"`
		LastName     string   `xml:"lastName"`
		EmailAddress string   `xml:"emailAddress"`
		PhoneNumber  string   `xml:"phoneNumber"`
	}
	xmlReq := createCustomerXML{
		FirstName:    reqBody.FirstName,
		LastName:     reqBody.LastName,
		EmailAddress: reqBody.EmailAddress,
		PhoneNumber:  reqBody.PhoneNumber,
	}
	xmlBody, err := xml.Marshal(xmlReq)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	xmlBodyStr := `<?xml version=\"1.0\" encoding=\"UTF-8\"?>` + string(xmlBody)
	reqBackend, err := http.NewRequest("POST", cfg.Backend+"/customers", io.NopCloser(strings.NewReader(xmlBodyStr)))
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	reqBackend.Header.Set("Content-Type", "application/xml")
	reqBackend.SetBasicAuth(cfg.BackendUser, cfg.BackendPass)
	resp, err := client.Do(reqBackend)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	defer resp.Body.Close()
	if resp.StatusCode == http.StatusConflict {
		respondError(c, logger, http.StatusConflict, "A customer with this email already exists", "DUPLICATE_CUSTOMER", nil)
		return
	}
	if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", nil)
		return
	}
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	var x xmlCustomer
	err = xml.Unmarshal(body, &x)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	c.JSON(http.StatusCreated, xmlToCustomer(x))
}

// GetCustomerByIdHandler handles GET /api/v2/customers/:id
// Retrieves a single customer by ID from the backend and returns it as JSON.
func GetCustomerByIdHandler(c *gin.Context, cfg Config, client *http.Client, logger *log.Logger) {
	id := c.Param("id")
	reqBackend, err := http.NewRequest("GET", cfg.Backend+"/customers/"+id, nil)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	reqBackend.SetBasicAuth(cfg.BackendUser, cfg.BackendPass)
	resp, err := client.Do(reqBackend)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	defer resp.Body.Close()
	if resp.StatusCode == http.StatusNotFound {
		respondError(c, logger, http.StatusNotFound, "Customer not found", "NOT_FOUND", nil)
		return
	}
	if resp.StatusCode != http.StatusOK {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", nil)
		return
	}
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	var x xmlCustomer
	err = xml.Unmarshal(body, &x)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	c.JSON(http.StatusOK, xmlToCustomer(x))
}

// GetCustomerPointsHandler handles GET /api/v2/customers/:id/points
// Fetches and returns the current available points for a customer.
func GetCustomerPointsHandler(c *gin.Context, cfg Config, client *http.Client, logger *log.Logger) {
	id := c.Param("id")
	reqBackend, err := http.NewRequest("GET", cfg.Backend+"/customers/"+id, nil)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	reqBackend.SetBasicAuth(cfg.BackendUser, cfg.BackendPass)
	resp, err := client.Do(reqBackend)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	defer resp.Body.Close()
	if resp.StatusCode == http.StatusNotFound {
		respondError(c, logger, http.StatusNotFound, "Customer not found", "NOT_FOUND", nil)
		return
	}
	if resp.StatusCode != http.StatusOK {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", nil)
		return
	}
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	var x xmlCustomer
	err = xml.Unmarshal(body, &x)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"customerId":             x.CustomerId,
		"currentAvailablePoints": x.CurrentAvailablePoints,
	})
}

// AdjustCustomerPointsHandler handles POST /api/v2/customers/:id/points
// Adjusts or redeems customer points, then returns the updated points balance.
func AdjustCustomerPointsHandler(c *gin.Context, cfg Config, client *http.Client, logger *log.Logger) {
	id := c.Param("id")
	var reqBody struct {
		PointsDelta int    `json:"pointsDelta"`
		Reason      string `json:"reason"`
	}
	if err := c.ShouldBindJSON(&reqBody); err != nil {
		respondError(c, logger, http.StatusBadRequest, "Invalid request payload", "BAD_REQUEST", err)
		return
	}
	var txnType string
	points := reqBody.PointsDelta
	if reqBody.PointsDelta > 0 {
		txnType = "ADJUST"
	} else if reqBody.PointsDelta < 0 {
		txnType = "REDEEM"
		points = -points
	} else {
		respondError(c, logger, http.StatusBadRequest, "pointsDelta must be non-zero", "BAD_REQUEST", nil)
		return
	}
	type transactionXML struct {
		XMLName         xml.Name `xml:"transaction"`
		CustomerId      string   `xml:"customerId"`
		TransactionType string   `xml:"transactionType"`
		PointsAmount    int      `xml:"pointsAmount"`
		Description     string   `xml:"description"`
	}
	xmlReq := transactionXML{
		CustomerId:      id,
		TransactionType: txnType,
		PointsAmount:    points,
		Description:     reqBody.Reason,
	}
	xmlBody, err := xml.Marshal(xmlReq)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	xmlBodyStr := `<?xml version="1.0" encoding="UTF-8"?>` + string(xmlBody)
	reqBackend, err := http.NewRequest("POST", cfg.Backend+"/transactions", io.NopCloser(strings.NewReader(xmlBodyStr)))
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	reqBackend.Header.Set("Content-Type", "application/xml")
	reqBackend.SetBasicAuth(cfg.BackendUser, cfg.BackendPass)
	postStart := NowMillis()
	resp, err := client.Do(reqBackend)
	postEnd := NowMillis()
	logger.Printf("[PERF] POST /transactions took %d ms", postEnd-postStart)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", nil)
		return
	}
	// After posting, fetch updated points
	req2, err := http.NewRequest("GET", cfg.Backend+"/customers/"+id, nil)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	req2.SetBasicAuth(cfg.BackendUser, cfg.BackendPass)
	getStart := NowMillis()
	resp2, err := client.Do(req2)
	getEnd := NowMillis()
	logger.Printf("[PERF] GET /customers/%s took %d ms", id, getEnd-getStart)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	defer resp2.Body.Close()
	if resp2.StatusCode == http.StatusNotFound {
		respondError(c, logger, http.StatusNotFound, "Customer not found", "NOT_FOUND", nil)
		return
	}
	if resp2.StatusCode != http.StatusOK {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", nil)
		return
	}
	body, err := io.ReadAll(resp2.Body)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	var x xmlCustomer
	err = xml.Unmarshal(body, &x)
	if err != nil {
		respondError(c, logger, http.StatusInternalServerError, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"customerId":                x.CustomerId,
		"newCurrentAvailablePoints": x.CurrentAvailablePoints,
		"pointsDelta":               reqBody.PointsDelta,
	})
}

// =========================
// Structs (API and XML Models)
// =========================

// ErrorResponse represents a standard error response body
type ErrorResponse struct {
	Message string `json:"message"`
	Code    string `json:"code"`
}

// Pagination contains pagination metadata for list responses
type Pagination struct {
	Offset int `json:"offset"`
	Limit  int `json:"limit"`
	Total  int `json:"total"`
}

// Customer represents a customer record in the API response
type Customer struct {
	CustomerId             string `json:"customerId"`
	FirstName              string `json:"firstName"`
	LastName               string `json:"lastName"`
	EmailAddress           string `json:"emailAddress"`
	PhoneNumber            string `json:"phoneNumber"`
	RegistrationDate       string `json:"registrationDate"`
	LoyaltyTier            string `json:"loyaltyTier"`
	TotalLifetimePoints    int    `json:"totalLifetimePoints"`
	CurrentAvailablePoints int    `json:"currentAvailablePoints"`
	AccountStatus          string `json:"accountStatus"`
}

// CustomersResponse is the response for listing customers
type CustomersResponse struct {
	Customers  []Customer `json:"customers"`
	Pagination Pagination `json:"pagination"`
}

// xmlCustomers is used for unmarshalling backend XML customer list
type xmlCustomers struct {
	XMLName   xml.Name      `xml:"customers"`
	Customers []xmlCustomer `xml:"customer"`
}

// xmlCustomer is used for unmarshalling backend XML for a single customer
type xmlCustomer struct {
	CustomerId             string `xml:"customerId"`
	FirstName              string `xml:"firstName"`
	LastName               string `xml:"lastName"`
	EmailAddress           string `xml:"emailAddress"`
	PhoneNumber            string `xml:"phoneNumber"`
	RegistrationDate       string `xml:"registrationDate"`
	LoyaltyTier            string `xml:"loyaltyTier"`
	TotalLifetimePoints    int    `xml:"totalLifetimePoints"`
	CurrentAvailablePoints int    `xml:"currentAvailablePoints"`
	AccountStatus          string `xml:"accountStatus"`
}

// =========================
// Helper Functions
// =========================

// xmlToCustomer converts xmlCustomer to API Customer struct
func xmlToCustomer(x xmlCustomer) Customer {
	return Customer{
		CustomerId:             x.CustomerId,
		FirstName:              x.FirstName,
		LastName:               x.LastName,
		EmailAddress:           x.EmailAddress,
		PhoneNumber:            x.PhoneNumber,
		RegistrationDate:       x.RegistrationDate,
		LoyaltyTier:            x.LoyaltyTier,
		TotalLifetimePoints:    x.TotalLifetimePoints,
		CurrentAvailablePoints: x.CurrentAvailablePoints,
		AccountStatus:          x.AccountStatus,
	}
}

// NowMillis returns the current time in milliseconds (for performance logging)
func NowMillis() int64 {
	return time.Now().UnixNano() / int64(time.Millisecond)
}

// respondError sends a consistent error response and logs the error
func respondError(c *gin.Context, logger *log.Logger, status int, message, code string, err error) {
	if err != nil {
		logger.Printf("[ERROR] %s: %v", message, err)
	} else {
		logger.Printf("[ERROR] %s", message)
	}
	c.JSON(status, ErrorResponse{Message: message, Code: code})
}
