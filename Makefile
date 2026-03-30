.PHONY: help dev test build native docker docker-native run run-native clean

# Default target
help:
	@echo "Available targets:"
	@echo "  dev          - Run in development mode (hot reload)"
	@echo "  test         - Run all tests"
	@echo "  build        - Build JVM package"
	@echo "  native       - Build native image"
	@echo "  docker       - Build Docker image (JVM)"
	@echo "  docker-native- Build Docker image (Native)"
	@echo "  run          - Run JVM version"
	@echo "  run-native   - Run native version"
	@echo "  cicd-test    - Run CICD validation tests"
	@echo "  clean        - Clean build artifacts"

# Development
dev:
	./mvnw quarkus:dev

# Testing
test:
	./mvnw clean test

integration-test:
	./mvnw verify

cicd-test:
	./mvnw test -Dtest=CICDIntegrationTest

# Building
build:
	./mvnw clean package -DskipTests

native:
	./mvnw package -Dnative -DskipTests

# Docker
docker:
	docker build --target jvm -t sklearn-model-validator:jvm .

docker-native:
	docker build -f Dockerfile.native -t sklearn-model-validator:native .

# Running
run: build
	java -jar target/quarkus-app/quarkus-run.jar

run-native: native
	./target/sklearn-model-validator-1.0.0-SNAPSHOT-runner

# CICD validation
cicd-validate:
	@echo "Starting application for CICD validation..."
	@java -jar target/quarkus-app/quarkus-run.jar &
	@sleep 15
	@./scripts/cicd-test.sh || (pkill -f quarkus-run.jar; exit 1)
	@pkill -f quarkus-run.jar

# Cleaning
clean:
	./mvnw clean
	rm -rf target/

# Full CICD pipeline simulation
cicd-pipeline: clean test build cicd-validate
	@echo "CICD pipeline completed successfully!"
