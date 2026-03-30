#!/bin/bash

# CICD Test Script for sklearn-model-validator
# This script can be used in any CICD platform

set -e

APP_HOST="${APP_HOST:-localhost}"
APP_PORT="${APP_PORT:-8080}"
BASE_URL="http://${APP_HOST}:${APP_PORT}"
MAX_RETRIES=30
RETRY_DELAY=2

echo "======================================"
echo "CICD Model Validation Test Suite"
echo "======================================"
echo "Testing against: ${BASE_URL}"
echo ""

# Function to wait for application
wait_for_app() {
    echo "Waiting for application to start..."
    local retries=0
    while [ $retries -lt $MAX_RETRIES ]; do
        if curl -sf "${BASE_URL}/health/ready" > /dev/null 2>&1; then
            echo "✓ Application is ready"
            return 0
        fi
        retries=$((retries + 1))
        echo "  Attempt $retries/$MAX_RETRIES - waiting ${RETRY_DELAY}s..."
        sleep $RETRY_DELAY
    done
    echo "✗ Application failed to start"
    return 1
}

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4

    echo -n "Testing: $description... "

    local response
    local http_code

    if [ -n "$data" ]; then
        response=$(curl -sf -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            -w "\n%{http_code}" \
            "${BASE_URL}${endpoint}" 2>/dev/null) || true
    else
        response=$(curl -sf -X "$method" \
            -w "\n%{http_code}" \
            "${BASE_URL}${endpoint}" 2>/dev/null) || true
    fi

    http_code=$(echo "$response" | tail -n1)

    if [ "$http_code" = "200" ]; then
        echo "✓ PASSED (HTTP $http_code)"
        return 0
    else
        echo "✗ FAILED (HTTP ${http_code:-no response})"
        return 1
    fi
}

# Wait for application
wait_for_app

echo ""
echo "======================================"
echo "Running Tests"
echo "======================================"

# Track failures
FAILED=0

# Test 1: Health check
test_endpoint "GET" "/health/ready" "" "Health check (ready)" || FAILED=$((FAILED + 1))
test_endpoint "GET" "/health/live" "" "Health check (live)" || FAILED=$((FAILED + 1))
test_endpoint "GET" "/health" "" "Health check (full)" || FAILED=$((FAILED + 1))

# Test 2: List models
test_endpoint "GET" "/api/v1/models" "" "List models" || FAILED=$((FAILED + 1))

# Test 3: Get model metadata
test_endpoint "GET" "/api/v1/models/mock-model" "" "Get model metadata" || FAILED=$((FAILED + 1))

# Test 4: Validate model
test_endpoint "POST" "/api/v1/models/mock-model/validate" "" "Validate model" || FAILED=$((FAILED + 1))

# Test 5: Predict
test_endpoint "POST" "/api/v1/models/mock-model/predict" \
    '{"data":[{"feature1":1.0,"feature2":2.0,"feature3":3.0}]}' \
    "Model prediction" || FAILED=$((FAILED + 1))

# Test 6: Validate all models
test_endpoint "POST" "/api/v1/models/validate-all" "" "Validate all models" || FAILED=$((FAILED + 1))

# Test 7: Service stats
test_endpoint "GET" "/api/v1/models/stats" "" "Service stats" || FAILED=$((FAILED + 1))

# Test 8: Detailed status
test_endpoint "GET" "/health/status" "" "Detailed status" || FAILED=$((FAILED + 1))

echo ""
echo "======================================"
echo "Test Summary"
echo "======================================"

if [ $FAILED -eq 0 ]; then
    echo "✓ All tests PASSED"
    exit 0
else
    echo "✗ $FAILED test(s) FAILED"
    exit 1
fi
