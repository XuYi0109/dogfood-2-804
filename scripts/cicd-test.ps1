# CICD Test Script for sklearn-model-validator (PowerShell)
# This script can be used in Windows CICD platforms

param(
    [string]$AppHost = "localhost",
    [int]$AppPort = 8080,
    [int]$MaxRetries = 30,
    [int]$RetryDelay = 2
)

$BaseUrl = "http://${AppHost}:${AppPort}"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "CICD Model Validation Test Suite" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Testing against: $BaseUrl"
Write-Host ""

# Function to wait for application
function Wait-ForApp {
    Write-Host "Waiting for application to start..."
    $retries = 0
    while ($retries -lt $MaxRetries) {
        try {
            $response = Invoke-WebRequest -Uri "$BaseUrl/health/ready" -Method GET -UseBasicParsing -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host "✓ Application is ready" -ForegroundColor Green
                return $true
            }
        } catch {
            $retries++
            Write-Host "  Attempt $retries/$MaxRetries - waiting ${RetryDelay}s..."
            Start-Sleep -Seconds $RetryDelay
        }
    }
    Write-Host "✗ Application failed to start" -ForegroundColor Red
    return $false
}

# Function to test endpoint
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Data = "",
        [string]$Description
    )

    Write-Host -NoNewline "Testing: $Description... "

    try {
        $uri = "$BaseUrl$Endpoint"
        $headers = @{
            "Content-Type" = "application/json"
        }

        if ($Data) {
            $response = Invoke-WebRequest -Uri $uri -Method $Method -Headers $headers -Body $Data -UseBasicParsing -ErrorAction Stop
        } else {
            $response = Invoke-WebRequest -Uri $uri -Method $Method -UseBasicParsing -ErrorAction Stop
        }

        if ($response.StatusCode -eq 200) {
            Write-Host "✓ PASSED (HTTP $($response.StatusCode))" -ForegroundColor Green
            return $true
        } else {
            Write-Host "✗ FAILED (HTTP $($response.StatusCode))" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "✗ FAILED (Error: $($_.Exception.Message))" -ForegroundColor Red
        return $false
    }
}

# Wait for application
if (-not (Wait-ForApp)) {
    exit 1
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Running Tests" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

# Track failures
$failed = 0

# Test 1: Health check
if (-not (Test-Endpoint -Method "GET" -Endpoint "/health/ready" -Description "Health check (ready)")) { $failed++ }
if (-not (Test-Endpoint -Method "GET" -Endpoint "/health/live" -Description "Health check (live)")) { $failed++ }
if (-not (Test-Endpoint -Method "GET" -Endpoint "/health" -Description "Health check (full)")) { $failed++ }

# Test 2: List models
if (-not (Test-Endpoint -Method "GET" -Endpoint "/api/v1/models" -Description "List models")) { $failed++ }

# Test 3: Get model metadata
if (-not (Test-Endpoint -Method "GET" -Endpoint "/api/v1/models/mock-model" -Description "Get model metadata")) { $failed++ }

# Test 4: Validate model
if (-not (Test-Endpoint -Method "POST" -Endpoint "/api/v1/models/mock-model/validate" -Description "Validate model")) { $failed++ }

# Test 5: Predict
$predictData = '{"data":[{"feature1":1.0,"feature2":2.0,"feature3":3.0}]}'
if (-not (Test-Endpoint -Method "POST" -Endpoint "/api/v1/models/mock-model/predict" -Data $predictData -Description "Model prediction")) { $failed++ }

# Test 6: Validate all models
if (-not (Test-Endpoint -Method "POST" -Endpoint "/api/v1/models/validate-all" -Description "Validate all models")) { $failed++ }

# Test 7: Service stats
if (-not (Test-Endpoint -Method "GET" -Endpoint "/api/v1/models/stats" -Description "Service stats")) { $failed++ }

# Test 8: Detailed status
if (-not (Test-Endpoint -Method "GET" -Endpoint "/health/status" -Description "Detailed status")) { $failed++ }

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

if ($failed -eq 0) {
    Write-Host "✓ All tests PASSED" -ForegroundColor Green
    exit 0
} else {
    Write-Host "✗ $failed test(s) FAILED" -ForegroundColor Red
    exit 1
}
