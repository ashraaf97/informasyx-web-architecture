#!/bin/bash

# API Documentation and Discovery Script
# This script documents all discovered endpoints and tests basic connectivity

BASE_URL="http://localhost:8080"

echo "========================================="
echo "    API ENDPOINT DISCOVERY & TESTING   "
echo "    Informasyx Web Architecture        "
echo "========================================="
echo ""

echo "Base URL: $BASE_URL"
echo "Test Date: $(date)"
echo ""

echo "1. TESTING API ACCESSIBILITY"
echo "================================"

# Test basic connectivity
echo "Testing base connectivity..."
response=$(curl -s -w "%{http_code}" "$BASE_URL/" 2>/dev/null)
status="${response: -3}"
echo "Root endpoint status: $status"

# Test Swagger UI
echo "Testing Swagger UI..."
response=$(curl -s -w "%{http_code}" "$BASE_URL/swagger-ui/index.html" 2>/dev/null)
status="${response: -3}"
echo "Swagger UI status: $status"

echo ""
echo "2. DISCOVERED API ENDPOINTS"
echo "================================"

echo "Authentication Endpoints (/api/auth):"
echo "  POST /api/auth/signup       - User registration"
echo "  POST /api/auth/login        - User authentication" 
echo "  POST /api/auth/logout       - User logout"
echo "  PUT  /api/auth/change-password - Change user password"
echo "  POST /api/auth/verify-email - Email verification"
echo "  POST /api/auth/forgot-password - Password reset request"
echo "  POST /api/auth/reset-password - Password reset"

echo ""
echo "User Management Endpoints (/api/users) - Requires Authentication:"
echo "  GET    /api/users             - List all users (Admin+)"
echo "  GET    /api/users/{id}        - Get user by ID (Admin+)"
echo "  GET    /api/users/username/{username} - Get user by username (Admin+)"
echo "  POST   /api/users             - Create user (Super Admin)"
echo "  PUT    /api/users/{id}        - Update user (Admin+)"
echo "  DELETE /api/users/{id}        - Delete user (Super Admin)"
echo "  GET    /api/users/exists/{username} - Check username existence (Admin+)"

echo ""
echo "Admin Management Endpoints (/api/admin) - Requires Authentication:"
echo "  POST /api/admin/users         - Create user (Admin+)"
echo "  POST /api/admin/users/admin   - Create admin user (Super Admin)"
echo "  PUT  /api/admin/users/role    - Change user role (Super Admin)"

echo ""
echo "Person Management Endpoints (/api/persons) - Requires Authentication:"
echo "  GET    /api/persons           - List all persons (Admin+)"
echo "  GET    /api/persons/{id}      - Get person by ID (User+)"
echo "  POST   /api/persons           - Create person (Admin+)"
echo "  PUT    /api/persons/{id}      - Update person (User+)"
echo "  DELETE /api/persons/{id}      - Delete person (Super Admin)"
echo "  GET    /api/persons/search/lastName?lastName={name} - Search by last name (Admin+)"
echo "  GET    /api/persons/search/name?firstName={first}&lastName={last} - Search by full name (Admin+)"
echo "  GET    /api/persons/search/email?email={email} - Search by email content (Admin+)"

echo ""
echo "3. AUTHENTICATION & AUTHORIZATION"
echo "=================================="
echo "Authentication Method: JWT Bearer Token"
echo "Authorization Roles: USER, ADMIN, SUPER_ADMIN"
echo "Default Admin User: admin/admin (from Liquibase changelog)"

echo ""
echo "4. BASIC ENDPOINT TESTING"
echo "=================================="

# Test public endpoints
echo "Testing public authentication endpoints..."

endpoints=(
    "POST:/api/auth/login:Login endpoint"
    "POST:/api/auth/signup:Signup endpoint"
    "POST:/api/auth/forgot-password:Forgot password endpoint"
)

for endpoint_info in "${endpoints[@]}"; do
    IFS=':' read -ra ENDPOINT_PARTS <<< "$endpoint_info"
    method="${ENDPOINT_PARTS[0]}"
    path="${ENDPOINT_PARTS[1]}"
    description="${ENDPOINT_PARTS[2]}"
    
    echo -n "  $method $path - "
    
    if [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d '{}' \
            "$BASE_URL$path" 2>/dev/null)
    else
        response=$(curl -s -w "%{http_code}" "$BASE_URL$path" 2>/dev/null)
    fi
    
    status="${response: -3}"
    case $status in
        200) echo "✓ OK (200)" ;;
        400) echo "✓ Accepts requests, validates input (400)" ;;
        401) echo "✓ Protected endpoint (401)" ;;
        403) echo "✓ Access forbidden (403)" ;;
        404) echo "✗ Not found (404)" ;;
        405) echo "✗ Method not allowed (405)" ;;
        500) echo "✗ Server error (500)" ;;
        *) echo "Status: $status" ;;
    esac
done

# Test protected endpoints (should return 401/403)
echo ""
echo "Testing protected endpoints (expecting 401/403)..."

protected_endpoints=(
    "GET:/api/users:User list"
    "GET:/api/persons:Person list"
    "POST:/api/admin/users:Admin user creation"
)

for endpoint_info in "${protected_endpoints[@]}"; do
    IFS=':' read -ra ENDPOINT_PARTS <<< "$endpoint_info"
    method="${ENDPOINT_PARTS[0]}"
    path="${ENDPOINT_PARTS[1]}"
    description="${ENDPOINT_PARTS[2]}"
    
    echo -n "  $method $path - "
    
    if [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d '{}' \
            "$BASE_URL$path" 2>/dev/null)
    else
        response=$(curl -s -w "%{http_code}" "$BASE_URL$path" 2>/dev/null)
    fi
    
    status="${response: -3}"
    case $status in
        401|403) echo "✓ Properly protected ($status)" ;;
        200) echo "⚠ Unexpectedly accessible (200)" ;;
        400) echo "⚠ Accessible but validates input (400)" ;;
        *) echo "Status: $status" ;;
    esac
done

echo ""
echo "5. REQUEST/RESPONSE SCHEMA ANALYSIS"
echo "===================================="

echo "Authentication Request Schema (Login):"
echo "{"
echo '  "username": "string (required)",'
echo '  "password": "string (required)"'
echo "}"
echo ""

echo "Authentication Response Schema:"
echo "{"
echo '  "token": "string | null",'
echo '  "username": "string | null",'
echo '  "message": "string",'
echo '  "success": "boolean",'
echo '  "role": "Role enum | null"'
echo "}"
echo ""

echo "User Registration Request Schema:"
echo "{"
echo '  "username": "string (3-50 chars, required)",'
echo '  "email": "string (valid email, required)",'
echo '  "firstName": "string (max 50 chars, required)",'
echo '  "lastName": "string (max 50 chars, required)",'
echo '  "phoneNumber": "string (max 20 chars, optional)",'
echo '  "address": "string (max 255 chars, optional)",'
echo '  "password": "string (complex pattern, required)",'
echo '  "confirmPassword": "string (required)"'
echo "}"
echo ""

echo "6. SECURITY FEATURES IDENTIFIED"
echo "================================"
echo "✓ JWT-based authentication"
echo "✓ Role-based authorization (USER, ADMIN, SUPER_ADMIN)"
echo "✓ Password complexity validation"
echo "✓ CORS configuration"
echo "✓ CSRF protection disabled (stateless API)"
echo "✓ Request validation with Jakarta Validation"
echo "✓ BCrypt password hashing"

echo ""
echo "7. TECHNOLOGY STACK ANALYSIS"
echo "============================="
echo "✓ Spring Boot 3.3.1"
echo "✓ Spring Security 6.x"
echo "✓ Spring Data JPA"
echo "✓ Hibernate ORM"
echo "✓ Liquibase for database migrations"
echo "✓ OpenAPI 3.0 (SpringDoc)"
echo "✓ H2/PostgreSQL database support"
echo "✓ Apache Kafka integration"
echo "✓ AWS SES email support"
echo "✓ MapStruct for DTO mapping"

echo ""
echo "8. API DESIGN PATTERNS"
echo "======================"
echo "✓ RESTful API design"
echo "✓ Consistent error responses"
echo "✓ Proper HTTP status codes"
echo "✓ Resource-based URL structure"
echo "✓ Stateless authentication"
echo "✓ DTO pattern for data transfer"
echo "✓ Repository pattern for data access"

echo ""
echo "9. PERFORMANCE CONSIDERATIONS"
echo "============================="

echo "Testing basic response times..."
start_time=$(date +%s%3N)
for i in {1..5}; do
    curl -s -o /dev/null "$BASE_URL/swagger-ui/index.html"
done
end_time=$(date +%s%3N)
avg_time=$(( (end_time - start_time) / 5 ))
echo "Average static resource response time: ${avg_time}ms"

echo ""
echo "10. RECOMMENDATIONS"
echo "==================="
echo "Immediate Actions:"
echo "  • Verify admin user credentials or create test user"
echo "  • Implement API rate limiting"
echo "  • Add request/response logging"
echo "  • Set up health check endpoints"

echo ""
echo "Medium-term Improvements:"
echo "  • Add API versioning strategy"
echo "  • Implement comprehensive integration tests"
echo "  • Set up performance monitoring"
echo "  • Add caching layer for read operations"

echo ""
echo "Long-term Enhancements:"
echo "  • Consider API gateway implementation"
echo "  • Add comprehensive audit logging"
echo "  • Implement advanced security features"
echo "  • Set up automated load testing"

echo ""
echo "==========================================="
echo "    API DISCOVERY COMPLETED              "
echo "==========================================="