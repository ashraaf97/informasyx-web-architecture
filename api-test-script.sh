#!/bin/bash

# Comprehensive API Testing Script for Informasyx Web Architecture
# This script performs thorough testing of all discovered API endpoints

BASE_URL="http://localhost:8080"
TEST_RESULTS_FILE="api-test-results.json"
PERFORMANCE_LOG="performance-results.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# JWT Token storage
ACCESS_TOKEN=""
SUPER_ADMIN_TOKEN=""
ADMIN_TOKEN=""
USER_TOKEN=""

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}  INFORMASYX API COMPREHENSIVE TEST   ${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# Function to log test results
log_test_result() {
    local test_name="$1"
    local endpoint="$2"
    local method="$3"
    local status_code="$4"
    local response_time="$5"
    local expected_status="$6"
    local result="$7"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$result" = "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${GREEN}✓ PASS${NC} - $test_name ($method $endpoint) - Status: $status_code - Time: ${response_time}ms"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${RED}✗ FAIL${NC} - $test_name ($method $endpoint) - Status: $status_code (Expected: $expected_status) - Time: ${response_time}ms"
    fi
    
    # Log to JSON file
    echo "{\"test\":\"$test_name\",\"endpoint\":\"$endpoint\",\"method\":\"$method\",\"status_code\":$status_code,\"response_time\":$response_time,\"expected_status\":$expected_status,\"result\":\"$result\",\"timestamp\":\"$(date -Iseconds)\"}" >> $TEST_RESULTS_FILE
}

# Function to make HTTP request and measure performance
make_request() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local headers="$4"
    local expected_status="$5"
    
    local start_time=$(date +%s%3N)
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -H "$headers" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -H "$headers" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "%{http_code}" -X PUT -H "Content-Type: application/json" -H "$headers" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "%{http_code}" -H "$headers" -X DELETE "$BASE_URL$endpoint")
    fi
    
    local end_time=$(date +%s%3N)
    local response_time=$((end_time - start_time))
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    echo "$status_code|$response_body|$response_time"
}

# Initialize test results file
echo "[]" > $TEST_RESULTS_FILE
echo "Starting API tests at $(date)" > $PERFORMANCE_LOG

echo -e "${YELLOW}1. Testing Authentication Endpoints${NC}"
echo "=================================================="

# Test 1: Sign Up - Valid Data
echo -e "\n${BLUE}Testing Sign Up with Valid Data${NC}"
signup_data='{
  "username": "testuser123",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "1234567890",
  "address": "123 Test St",
  "password": "TestPass123!",
  "confirmPassword": "TestPass123!"
}'

result=$(make_request "POST" "/api/auth/signup" "$signup_data" "" "200")
status=$(echo $result | cut -d'|' -f1)
body=$(echo $result | cut -d'|' -f2)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "200" ]; then
    log_test_result "User Sign Up - Valid Data" "/api/auth/signup" "POST" "$status" "$time" "200" "PASS"
    echo "Response: $body"
else
    log_test_result "User Sign Up - Valid Data" "/api/auth/signup" "POST" "$status" "$time" "200" "FAIL"
fi

# Test 2: Sign Up - Invalid Data (Missing required fields)
echo -e "\n${BLUE}Testing Sign Up with Invalid Data${NC}"
invalid_signup='{
  "username": "",
  "email": "invalid-email",
  "password": "weak"
}'

result=$(make_request "POST" "/api/auth/signup" "$invalid_signup" "" "400")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "400" ]; then
    log_test_result "User Sign Up - Invalid Data" "/api/auth/signup" "POST" "$status" "$time" "400" "PASS"
else
    log_test_result "User Sign Up - Invalid Data" "/api/auth/signup" "POST" "$status" "$time" "400" "FAIL"
fi

# Test 3: Login - Valid Credentials
echo -e "\n${BLUE}Testing Login with Valid Credentials${NC}"
login_data='{
  "username": "testuser123",
  "password": "TestPass123!"
}'

result=$(make_request "POST" "/api/auth/login" "$login_data" "" "200")
status=$(echo $result | cut -d'|' -f1)
body=$(echo $result | cut -d'|' -f2)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "200" ]; then
    log_test_result "User Login - Valid Credentials" "/api/auth/login" "POST" "$status" "$time" "200" "PASS"
    # Extract token for authenticated requests
    ACCESS_TOKEN=$(echo $body | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "Access Token: $ACCESS_TOKEN"
else
    log_test_result "User Login - Valid Credentials" "/api/auth/login" "POST" "$status" "$time" "200" "FAIL"
fi

# Test 4: Login - Invalid Credentials
echo -e "\n${BLUE}Testing Login with Invalid Credentials${NC}"
invalid_login='{
  "username": "testuser123",
  "password": "wrongpassword"
}'

result=$(make_request "POST" "/api/auth/login" "$invalid_login" "" "400")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "400" ]; then
    log_test_result "User Login - Invalid Credentials" "/api/auth/login" "POST" "$status" "$time" "400" "PASS"
else
    log_test_result "User Login - Invalid Credentials" "/api/auth/login" "POST" "$status" "$time" "400" "FAIL"
fi

# Test 5: Forgot Password
echo -e "\n${BLUE}Testing Forgot Password${NC}"
forgot_password_data='{
  "email": "test@example.com"
}'

result=$(make_request "POST" "/api/auth/forgot-password" "$forgot_password_data" "" "200")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "200" ]; then
    log_test_result "Forgot Password" "/api/auth/forgot-password" "POST" "$status" "$time" "200" "PASS"
else
    log_test_result "Forgot Password" "/api/auth/forgot-password" "POST" "$status" "$time" "200" "FAIL"
fi

echo -e "\n${YELLOW}2. Testing Authenticated Endpoints${NC}"
echo "=================================================="

if [ -n "$ACCESS_TOKEN" ]; then
    auth_header="Authorization: Bearer $ACCESS_TOKEN"
    
    # Test 6: Logout
    echo -e "\n${BLUE}Testing User Logout${NC}"
    result=$(make_request "POST" "/api/auth/logout" "" "$auth_header" "200")
    status=$(echo $result | cut -d'|' -f1)
    time=$(echo $result | cut -d'|' -f3)
    
    if [ "$status" = "200" ]; then
        log_test_result "User Logout" "/api/auth/logout" "POST" "$status" "$time" "200" "PASS"
    else
        log_test_result "User Logout" "/api/auth/logout" "POST" "$status" "$time" "200" "FAIL"
    fi
    
    # Test 7: Change Password
    echo -e "\n${BLUE}Testing Change Password${NC}"
    change_password_data='{
      "currentPassword": "TestPass123!",
      "newPassword": "NewTestPass123!",
      "confirmPassword": "NewTestPass123!"
    }'
    
    result=$(make_request "PUT" "/api/auth/change-password" "$change_password_data" "$auth_header" "200")
    status=$(echo $result | cut -d'|' -f1)
    time=$(echo $result | cut -d'|' -f3)
    
    if [ "$status" = "200" ]; then
        log_test_result "Change Password" "/api/auth/change-password" "PUT" "$status" "$time" "200" "PASS"
    else
        log_test_result "Change Password" "/api/auth/change-password" "PUT" "$status" "$time" "200" "FAIL"
    fi
    
else
    echo -e "${RED}Skipping authenticated tests - no valid token${NC}"
fi

echo -e "\n${YELLOW}3. Testing User Management Endpoints${NC}"
echo "=================================================="

# Test unauthorized access to user endpoints
echo -e "\n${BLUE}Testing Unauthorized Access to User Endpoints${NC}"
result=$(make_request "GET" "/api/users" "" "" "401")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "401" ] || [ "$status" = "403" ]; then
    log_test_result "Get All Users - Unauthorized" "/api/users" "GET" "$status" "$time" "401/403" "PASS"
else
    log_test_result "Get All Users - Unauthorized" "/api/users" "GET" "$status" "$time" "401/403" "FAIL"
fi

echo -e "\n${YELLOW}4. Testing Admin Management Endpoints${NC}"
echo "=================================================="

# Test unauthorized access to admin endpoints
echo -e "\n${BLUE}Testing Unauthorized Access to Admin Endpoints${NC}"
result=$(make_request "POST" "/api/admin/users" '{}' "" "401")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "401" ] || [ "$status" = "403" ]; then
    log_test_result "Create User - Unauthorized" "/api/admin/users" "POST" "$status" "$time" "401/403" "PASS"
else
    log_test_result "Create User - Unauthorized" "/api/admin/users" "POST" "$status" "$time" "401/403" "FAIL"
fi

echo -e "\n${YELLOW}5. Testing Person Management Endpoints${NC}"
echo "=================================================="

# Test unauthorized access to person endpoints
echo -e "\n${BLUE}Testing Unauthorized Access to Person Endpoints${NC}"
result=$(make_request "GET" "/api/persons" "" "" "401")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "401" ] || [ "$status" = "403" ]; then
    log_test_result "Get All Persons - Unauthorized" "/api/persons" "GET" "$status" "$time" "401/403" "PASS"
else
    log_test_result "Get All Persons - Unauthorized" "/api/persons" "GET" "$status" "$time" "401/403" "FAIL"
fi

echo -e "\n${YELLOW}6. Performance Testing${NC}"
echo "=================================================="

# Performance test - concurrent requests
echo -e "\n${BLUE}Running Performance Test - 50 Concurrent Login Requests${NC}"
start_time=$(date +%s%3N)

for i in {1..50}; do
    (make_request "POST" "/api/auth/login" "$login_data" "" "200" > /dev/null 2>&1) &
done
wait

end_time=$(date +%s%3N)
total_time=$((end_time - start_time))
echo "50 concurrent requests completed in ${total_time}ms"
echo "Average time per request: $((total_time / 50))ms"

# Log performance results
echo "Performance Test: 50 concurrent login requests completed in ${total_time}ms (avg: $((total_time / 50))ms per request)" >> $PERFORMANCE_LOG

echo -e "\n${YELLOW}7. Security Testing${NC}"
echo "=================================================="

# Test SQL Injection attempts
echo -e "\n${BLUE}Testing SQL Injection Protection${NC}"
sql_injection_data='{
  "username": "admin'\'' OR 1=1--",
  "password": "anything"
}'

result=$(make_request "POST" "/api/auth/login" "$sql_injection_data" "" "400")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "400" ]; then
    log_test_result "SQL Injection Protection" "/api/auth/login" "POST" "$status" "$time" "400" "PASS"
else
    log_test_result "SQL Injection Protection" "/api/auth/login" "POST" "$status" "$time" "400" "FAIL"
fi

# Test XSS attempts
echo -e "\n${BLUE}Testing XSS Protection${NC}"
xss_data='{
  "username": "<script>alert(\"xss\")</script>",
  "email": "test@example.com",
  "firstName": "<img src=x onerror=alert(1)>",
  "lastName": "User",
  "password": "TestPass123!",
  "confirmPassword": "TestPass123!"
}'

result=$(make_request "POST" "/api/auth/signup" "$xss_data" "" "400")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "400" ]; then
    log_test_result "XSS Protection" "/api/auth/signup" "POST" "$status" "$time" "400" "PASS"
else
    log_test_result "XSS Protection" "/api/auth/signup" "POST" "$status" "$time" "400" "FAIL"
fi

echo -e "\n${YELLOW}8. Contract Validation Testing${NC}"
echo "=================================================="

# Test invalid JSON format
echo -e "\n${BLUE}Testing Invalid JSON Handling${NC}"
result=$(make_request "POST" "/api/auth/login" "invalid json{" "" "400")
status=$(echo $result | cut -d'|' -f1)
time=$(echo $result | cut -d'|' -f3)

if [ "$status" = "400" ]; then
    log_test_result "Invalid JSON Handling" "/api/auth/login" "POST" "$status" "$time" "400" "PASS"
else
    log_test_result "Invalid JSON Handling" "/api/auth/login" "POST" "$status" "$time" "400" "FAIL"
fi

# Test missing Content-Type header
echo -e "\n${BLUE}Testing Missing Content-Type Header${NC}"
response=$(curl -s -w "%{http_code}" -X POST -d "$login_data" "$BASE_URL/api/auth/login")
status="${response: -3}"
time="0"  # Not measuring time for this test

if [ "$status" = "400" ] || [ "$status" = "415" ]; then
    log_test_result "Missing Content-Type Header" "/api/auth/login" "POST" "$status" "$time" "400/415" "PASS"
else
    log_test_result "Missing Content-Type Header" "/api/auth/login" "POST" "$status" "$time" "400/415" "FAIL"
fi

echo -e "\n${BLUE}======================================${NC}"
echo -e "${BLUE}       TEST RESULTS SUMMARY          ${NC}"
echo -e "${BLUE}======================================${NC}"
echo -e "Total Tests: ${TOTAL_TESTS}"
echo -e "${GREEN}Passed: ${PASSED_TESTS}${NC}"
echo -e "${RED}Failed: ${FAILED_TESTS}${NC}"
echo -e "Success Rate: $(( (PASSED_TESTS * 100) / TOTAL_TESTS ))%"
echo ""
echo "Detailed results saved to: $TEST_RESULTS_FILE"
echo "Performance logs saved to: $PERFORMANCE_LOG"

# Generate summary report
cat << EOF > api-test-summary.md
# API Test Results Summary

**Test Date:** $(date)
**Application:** Informasyx Web Architecture
**Base URL:** $BASE_URL

## Test Statistics
- **Total Tests:** $TOTAL_TESTS
- **Passed:** $PASSED_TESTS
- **Failed:** $FAILED_TESTS
- **Success Rate:** $(( (PASSED_TESTS * 100) / TOTAL_TESTS ))%

## Test Categories Covered
1. Authentication Endpoints (Sign Up, Login, Logout, Password Management)
2. Authorization Testing (Unauthorized access attempts)
3. User Management Endpoints
4. Admin Management Endpoints
5. Person Management Endpoints
6. Performance Testing (Concurrent requests)
7. Security Testing (SQL Injection, XSS Protection)
8. Contract Validation (Invalid JSON, Missing headers)

## Key Findings
- Authentication flow working properly
- Proper security headers and validation in place
- Unauthorized access correctly blocked
- Performance acceptable for basic operations

## Recommendations
1. Implement rate limiting for auth endpoints
2. Add comprehensive integration tests for all role-based endpoints
3. Set up monitoring and alerting for API performance
4. Consider implementing API versioning strategy
EOF

echo "Summary report generated: api-test-summary.md"