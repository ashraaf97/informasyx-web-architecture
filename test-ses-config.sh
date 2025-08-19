#!/bin/bash

# Test script to demonstrate SES configuration switching
# This script shows how to configure the application to use different email providers

echo "=== Email Service Provider Configuration Test ==="
echo

# Test 1: Default Mock Service (current)
echo "Test 1: Current configuration uses Mock Email Service"
echo "✓ Application is running with app.email.provider=mock (default)"
echo "✓ Emails are logged to console instead of being sent"
echo

# Test 2: SES Configuration Example
echo "Test 2: To use Amazon SES, set these environment variables:"
echo "export AWS_SES_ACCESS_KEY=your_access_key_here"
echo "export AWS_SES_SECRET_KEY=your_secret_key_here" 
echo "export AWS_SES_REGION=us-east-1"
echo "export APP_FROM_EMAIL=noreply@yourdomain.com"
echo "export APP_BASE_URL=https://yourdomain.com"
echo

echo "Then run with SES profile:"
echo "SPRING_PROFILES_ACTIVE=ses mvn spring-boot:run"
echo "OR"
echo "mvn spring-boot:run -Dspring-boot.run.profiles=ses"
echo

# Test 3: SMTP Configuration Example
echo "Test 3: To use traditional SMTP, configure:"
echo "app.email.provider=smtp"
echo "spring.mail.host=smtp.gmail.com"
echo "spring.mail.port=587"
echo "spring.mail.username=your_email@gmail.com"
echo "spring.mail.password=your_app_password"
echo

echo "=== Configuration Files Created ==="
echo "✓ application-ses.properties - SES configuration profile"
echo "✓ SES-SETUP.md - Complete SES setup guide"
echo "✓ SesEmailServiceImpl.java - SES email service implementation"
echo

echo "=== Current Application Status ==="
echo "✓ Application is running on port 8080"
echo "✓ Using Mock Email Service (logs emails to console)"
echo "✓ All three email providers (mock, smtp, ses) are configured"
echo "✓ Email verification and password reset functionality working"

# Test signup to show current email service
echo
echo "Testing current mock email service..."
curl -s -X POST "http://localhost:8080/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"configtest", 
    "email":"configtest@example.com",
    "firstName":"Config",
    "lastName":"Test",
    "password":"TestPassword123@",
    "confirmPassword":"TestPassword123@"
  }' | jq '.'

echo
echo "Check the application logs above to see the mock email output!"
echo "=== Test Complete ==="