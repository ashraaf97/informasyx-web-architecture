// k6 Load Testing Script for Informasyx Web Architecture API
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 }, // Ramp up to 10 users over 30 seconds
    { duration: '1m', target: 10 },  // Stay at 10 users for 1 minute
    { duration: '30s', target: 20 }, // Ramp up to 20 users
    { duration: '1m', target: 20 },  // Stay at 20 users for 1 minute
    { duration: '30s', target: 0 },  // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95% of requests must complete below 1s
    http_req_failed: ['rate<0.1'],     // Error rate must be below 10%
    errors: ['rate<0.1'],              // Custom error rate below 10%
  },
};

const BASE_URL = 'http://localhost:8080';

// Test data
const testUsers = [
  { username: 'testuser1', password: 'TestPass123!' },
  { username: 'testuser2', password: 'TestPass123!' },
  { username: 'admin', password: 'admin' }
];

export default function () {
  // Test 1: Health check (via Swagger UI)
  let response = http.get(`${BASE_URL}/swagger-ui/index.html`);
  check(response, {
    'swagger UI is available': (r) => r.status === 200,
  }) || errorRate.add(1);

  // Test 2: Authentication endpoints
  const randomUser = testUsers[Math.floor(Math.random() * testUsers.length)];
  
  // Login attempt
  const loginPayload = JSON.stringify({
    username: randomUser.username,
    password: randomUser.password
  });

  const loginResponse = http.post(
    `${BASE_URL}/api/auth/login`,
    loginPayload,
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  const loginSuccess = check(loginResponse, {
    'login endpoint responds': (r) => r.status === 200 || r.status === 400,
    'login response time < 500ms': (r) => r.timings.duration < 500,
  });

  if (!loginSuccess) {
    errorRate.add(1);
  }

  // Test 3: Signup endpoint stress test
  const signupPayload = JSON.stringify({
    username: `user_${Math.random().toString(36).substring(7)}`,
    email: `test_${Math.random().toString(36).substring(7)}@example.com`,
    firstName: 'Load',
    lastName: 'Test',
    password: 'TestPass123!',
    confirmPassword: 'TestPass123!'
  });

  const signupResponse = http.post(
    `${BASE_URL}/api/auth/signup`,
    signupPayload,
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(signupResponse, {
    'signup endpoint responds': (r) => r.status === 200 || r.status === 400,
    'signup response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  // Test 4: Forgot password endpoint
  const forgotPasswordPayload = JSON.stringify({
    email: 'test@example.com'
  });

  const forgotPasswordResponse = http.post(
    `${BASE_URL}/api/auth/forgot-password`,
    forgotPasswordPayload,
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(forgotPasswordResponse, {
    'forgot password responds correctly': (r) => r.status === 200,
    'forgot password response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  // Test 5: Protected endpoints (should return 401/403)
  const protectedResponse = http.get(`${BASE_URL}/api/users`);
  
  check(protectedResponse, {
    'protected endpoint properly secured': (r) => r.status === 401 || r.status === 403,
    'security response time < 200ms': (r) => r.timings.duration < 200,
  }) || errorRate.add(1);

  // Test 6: Invalid JSON handling
  const invalidJsonResponse = http.post(
    `${BASE_URL}/api/auth/login`,
    'invalid json{',
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(invalidJsonResponse, {
    'invalid JSON properly rejected': (r) => r.status === 400,
  }) || errorRate.add(1);

  // Small delay to avoid overwhelming the server
  sleep(0.1);
}

// Setup function - runs once before the test
export function setup() {
  console.log('Starting load test for Informasyx Web Architecture API');
  console.log(`Base URL: ${BASE_URL}`);
  
  // Verify the application is running
  const healthCheck = http.get(`${BASE_URL}/swagger-ui/index.html`);
  if (healthCheck.status !== 200) {
    throw new Error('Application is not responding properly. Health check failed.');
  }
  
  return { baseUrl: BASE_URL };
}

// Teardown function - runs once after the test
export function teardown(data) {
  console.log('Load test completed');
}