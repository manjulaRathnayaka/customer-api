# Customer API

This service provides a RESTful API for managing customer data and loyalty points. It acts as a gateway between a modern JSON-based API and a legacy backend that communicates using XML. The service handles conversion between JSON and XML, and exposes endpoints for customer management and points operations.

**Note:** The backend system requires HTTP Basic Authentication. The API service uses credentials (username and password) to authenticate with the backend for all operations.
## Features
- List customers with filtering and pagination
- Create new customers
- Get customer details by ID
- Get and adjust customer points
- Converts between JSON (API) and XML (backend)
- Authenticates with the backend using HTTP Basic Auth (see Deployment Instructions)

## Example cURL Commands

### List Customers
```
curl -X GET "http://localhost:8080/api/v2/customers?firstName=John&limit=2" -H "accept: application/json"
```
**Response:**
```
{
  "customers": [
    {
      "customerId": "1",
      "firstName": "John",
      "lastName": "Doe",
      "emailAddress": "john.doe@example.com",
      "phoneNumber": "1234567890",
      "registrationDate": "2023-01-01",
      "loyaltyTier": "Gold",
      "totalLifetimePoints": 1000,
      "currentAvailablePoints": 200,
      "accountStatus": "ACTIVE"
    }
  ],
  "pagination": { "offset": 0, "limit": 2, "total": 1 }
}
```

### Create Customer
```
curl -X POST "http://localhost:8080/api/v2/customers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "emailAddress": "jane.smith@example.com",
    "phoneNumber": "9876543210"
  }'
```
**Response:**
```
{
  "customerId": "2",
  "firstName": "Jane",
  "lastName": "Smith",
  "emailAddress": "jane.smith@example.com",
  "phoneNumber": "9876543210",
  "registrationDate": "2023-01-02",
  "loyaltyTier": "Silver",
  "totalLifetimePoints": 0,
  "currentAvailablePoints": 0,
  "accountStatus": "ACTIVE"
}
```

### Get Customer by ID
```
curl -X GET "http://localhost:8080/api/v2/customers/1" -H "accept: application/json"
```
**Response:**
```
{
  "customerId": "1",
  "firstName": "John",
  ...
}
```

### Get Customer Points
```
curl -X GET "http://localhost:8080/api/v2/customers/1/points" -H "accept: application/json"
```
**Response:**
```
{
  "customerId": "1",
  "currentAvailablePoints": 200
}
```

### Adjust Customer Points
```
curl -X POST "http://localhost:8080/api/v2/customers/1/points" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "pointsDelta": 50,
    "reason": "Bonus points"
  }'
```
**Response:**
```
{
  "customerId": "1",
  "newCurrentAvailablePoints": 250,
  "pointsDelta": 50
}
```

## Deployment Instructions

1. Ensure you have Go installed (1.18+ recommended).
2. Clone the repository and navigate to the `customer-api` directory.
3. Build the service:
   ```
   make build
   ```
4. Run the service:
   ```
   make run
   ```
5. The API will be available at `http://localhost:8080` by default.

### Prerequisites
- Go 1.23 or later
- The backend service (Enterprise Customer Rewards System) must be running and accessible

### Environment Variables
The following environment variables must be set (see `.choreo/component.yaml`):

- `ENTERPRISE_CUSTOMER_REWARDS_BACKEND` - Backend base URL (default: `http://localhost:8080/enterprise-customer-rewards-system/service`)
- `ENTERPRISE_CUSTOMER_REWARDS_BACKEND_USER` - Backend username (default: `admin`)
- `ENTERPRISE_CUSTOMER_REWARDS_BACKEND_PASS` - Backend password (default: `admin123`)

### Steps
1. Clone the repository and navigate to the `customer-api` directory.
2. Build the service:
   ```sh
   make build
   ```
3. Run the service (ensure environment variables are set):
   ```sh
   make run
   ```
4. The API will be available at `http://localhost:8080` by default.
## Implementation Notes

- **XML to JSON Conversion:** The service receives XML from the backend, unmarshals it into Go structs, and then marshals it as JSON for API responses.
- **JSON to XML Conversion:** For POST requests, the service accepts JSON, converts it to Go structs, marshals it to XML, and sends it to the backend.
- **Filtering:** Query parameters like `firstName`, `lastName`, etc., are used to filter customers in-memory after fetching from the backend.
- **Pagination:** Supports `limit` and `offset` query parameters for paginated results.
- **Error Handling:** Consistent error responses with HTTP status codes and error codes.
- **Backend Authentication:** All requests to the backend are made using HTTP Basic Auth, with credentials provided via environment variables.

