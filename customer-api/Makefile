# Makefile for customer-api

.PHONY: build run test clean

BINARY=customer-api

build:
	go build -o ./bin/$(BINARY) main.go handlers.go

run: build
	./bin/$(BINARY)

test:
	go test ./...

clean:
	rm -rf ./bin