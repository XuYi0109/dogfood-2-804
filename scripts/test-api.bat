@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo CICD Model Service Test Script (Windows)
echo ==========================================

set BASE_URL=%1
if "%BASE_URL%"=="" set BASE_URL=http://localhost:8080

set TIMEOUT=%2
if "%TIMEOUT%"=="" set TIMEOUT=30

echo.
echo Starting tests against: %BASE_URL%
echo.

echo Waiting for service at %BASE_URL%...
set /a count=0
:wait_loop
curl -s -f "%BASE_URL%/q/health" >nul 2>&1
if %errorlevel%==0 goto service_ready
set /a count+=1
if %count% geq %TIMEOUT% (
    echo ERROR: Service did not start within %TIMEOUT% seconds
    exit /b 1
)
timeout /t 1 >nul
goto wait_loop

:service_ready
echo Service is ready!
echo.

echo Test 1: Health Check
echo --------------------
curl -s "%BASE_URL%/q/health"
echo.
if %errorlevel% neq 0 (
    echo Health check failed
    exit /b 1
)
echo.

echo Test 2: Model Status
echo --------------------
curl -s "%BASE_URL%/api/model/status"
echo.
echo.

echo Test 3: Model Validation
echo -------------------------
curl -s "%BASE_URL%/api/model/validate"
echo.
echo.

echo Test 4: Prediction
echo ------------------
curl -s -X POST -H "Content-Type: application/json" -d "{\"features\": [5.1, 3.5, 1.4, 0.2]}" "%BASE_URL%/api/predict"
echo.
echo.

echo Test 5: Model Info
echo ------------------
curl -s "%BASE_URL%/api/model/info"
echo.
echo.

echo ==========================================
echo All tests completed!
echo ==========================================
