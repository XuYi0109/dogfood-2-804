#!/bin/bash

set -e

echo "=========================================="
echo "CICD Model Service Test Script"
echo "=========================================="

BASE_URL="${1:-http://localhost:8080}"
TIMEOUT="${2:-30}"

wait_for_service() {
    echo "Waiting for service at $BASE_URL..."
    for i in $(seq 1 $TIMEOUT); do
        if curl -s -f "$BASE_URL/q/health" > /dev/null 2>&1; then
            echo "Service is ready!"
            return 0
        fi
        sleep 1
    done
    echo "ERROR: Service did not start within ${TIMEOUT} seconds"
    return 1
}

test_health() {
    echo ""
    echo "Test 1: Health Check"
    echo "--------------------"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/q/health")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✓ Health check passed (HTTP $HTTP_CODE)"
        echo "Response: $BODY"
    else
        echo "✗ Health check failed (HTTP $HTTP_CODE)"
        exit 1
    fi
}

test_model_status() {
    echo ""
    echo "Test 2: Model Status"
    echo "--------------------"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/model/status")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✓ Model status check passed (HTTP $HTTP_CODE)"
        echo "Response: $BODY"
        
        if echo "$BODY" | grep -q '"modelLoaded":true'; then
            echo "✓ Model is loaded"
        else
            echo "⚠ Model is not loaded"
        fi
    else
        echo "✗ Model status check failed (HTTP $HTTP_CODE)"
        exit 1
    fi
}

test_model_validate() {
    echo ""
    echo "Test 3: Model Validation"
    echo "-------------------------"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/model/validate")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✓ Model validation passed (HTTP $HTTP_CODE)"
        echo "Response: $BODY"
    else
        echo "✗ Model validation failed (HTTP $HTTP_CODE)"
        echo "Response: $BODY"
        exit 1
    fi
}

test_predict() {
    echo ""
    echo "Test 4: Prediction"
    echo "------------------"
    REQUEST='{"features": [5.1, 3.5, 1.4, 0.2]}'
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$REQUEST" \
        "$BASE_URL/api/predict")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✓ Prediction passed (HTTP $HTTP_CODE)"
        echo "Request: $REQUEST"
        echo "Response: $BODY"
        
        if echo "$BODY" | grep -q '"success":true'; then
            echo "✓ Prediction successful"
        else
            echo "✗ Prediction returned failure"
            exit 1
        fi
    else
        echo "✗ Prediction failed (HTTP $HTTP_CODE)"
        echo "Response: $BODY"
        exit 1
    fi
}

test_model_info() {
    echo ""
    echo "Test 5: Model Info"
    echo "------------------"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/model/info")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✓ Model info passed (HTTP $HTTP_CODE)"
        echo "Response: $BODY"
    else
        echo "✗ Model info failed (HTTP $HTTP_CODE)"
        exit 1
    fi
}

echo ""
echo "Starting tests against: $BASE_URL"
echo ""

wait_for_service
test_health
test_model_status
test_model_validate
test_predict
test_model_info

echo ""
echo "=========================================="
echo "All tests passed! ✓"
echo "=========================================="
