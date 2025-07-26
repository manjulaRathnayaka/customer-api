package main

import (
	"log"
	"net/http"
	"os"
	"time"

	"github.com/gin-gonic/gin"
)

type Config struct {
	Backend     string
	BackendUser string
	BackendPass string
}

func loadConfig() Config {
	cfg := Config{
		Backend:     os.Getenv("ENTERPRISE_CUSTOMER_REWARDS_BACKEND"),
		BackendUser: os.Getenv("ENTERPRISE_CUSTOMER_REWARDS_BACKEND_USER"),
		BackendPass: os.Getenv("ENTERPRISE_CUSTOMER_REWARDS_BACKEND_PASS"),
	}
	if cfg.Backend == "" {
		cfg.Backend = "http://localhost:8080/enterprise-customer-rewards-system/service"
	}
	if cfg.BackendUser == "" {
		cfg.BackendUser = "admin"
	}
	if cfg.BackendPass == "" {
		cfg.BackendPass = "admin123"
	}
	return cfg
}

func main() {
	logger := log.New(os.Stdout, "", log.LstdFlags)
	cfg := loadConfig()
	client := &http.Client{Timeout: 60 * time.Second}

	r := gin.Default()

	r.GET("/api/v2/customers", func(c *gin.Context) {
		ListCustomersHandler(c, cfg, client, logger)
	})
	r.POST("/api/v2/customers", func(c *gin.Context) {
		CreateCustomerHandler(c, cfg, client, logger)
	})
	r.GET("/api/v2/customers/:id", func(c *gin.Context) {
		GetCustomerByIdHandler(c, cfg, client, logger)
	})
	r.GET("/api/v2/customers/:id/reward-points", func(c *gin.Context) {
		GetCustomerPointsHandler(c, cfg, client, logger)
	})
	r.POST("/api/v2/customers/:id/reward-points", func(c *gin.Context) {
		AdjustCustomerPointsHandler(c, cfg, client, logger)
	})
	r.GET("/healthz", func(c *gin.Context) {
		c.String(http.StatusOK, "ok")
	})

	// Start Gin server on port 8080
	if err := r.Run(":8080"); err != nil {
		logger.Fatalf("failed to start server: %v", err)
	}
}
