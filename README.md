# Authentication & User Management System

This full-stack application provides authentication and user management functionality with a Spring Boot REST API backend and Angular frontend.

## Features

- **Authentication System**: Login/logout with JWT-like token-based authentication
- **User Registration & Email Verification**: Complete signup flow with email verification
- **Password Management**: Change password and forgot password with email reset
- **Multiple Email Providers**: Support for Mock, SMTP, and Amazon SES
- **Angular Frontend**: Modern web interface with comprehensive auth components
- **User Management**: CRUD operations for Person and User entities
- **Security**: Spring Security with BCrypt password hashing
- **Database Support**: PostgreSQL with Liquibase migrations
- **API Documentation**: OpenAPI/Swagger integration
- **Containerization**: Full Docker support with multi-service deployment
- **Admin User**: Pre-configured admin account for immediate access

## Requirements

- Java 17 or higher
- Maven 3.8 or higher
- Node.js 20 or higher (for Angular frontend)
- PostgreSQL (when not using Docker)
- Docker and Docker Compose (recommended for easy deployment)

## Running the Application

### Using Docker (Recommended)

#### With Docker Compose

1. Build and start all services:
   ```bash
   docker-compose up --build
   ```

2. Access the applications:
   - **Frontend (Angular)**: http://localhost:4200
   - **Backend API**: http://localhost:8080
   - **API Documentation**: http://localhost:8080/swagger-ui.html

3. **Login Credentials**:
   - Username: `admin`
   - Password: `admin`

4. To stop all services:
   ```bash
   docker-compose down
   ```

#### Services Overview

The Docker Compose setup includes:
- **Frontend**: Angular application served with Nginx (port 4200)
- **Backend**: Spring Boot API (port 8080)  
- **Database**: PostgreSQL (port 5432)

### Without Docker (Development Mode)

#### Backend (Spring Boot)

1. Navigate to the project root directory
2. Start PostgreSQL database (or use H2 by setting `spring.profiles.active=h2`)
3. Run the backend:
   ```bash
   mvn spring-boot:run
   ```

#### Frontend (Angular)

1. Navigate to the frontend directory:
   ```bash
   cd frontend/auth-app
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Access the application at http://localhost:4200

## Database Configuration

The application supports multiple database configurations:

### PostgreSQL

By default, the application uses PostgreSQL. Make sure you have a PostgreSQL server running with the following configuration:

- URL: `jdbc:postgresql://localhost:5432/demo`
- Username: `postgres`
- Password: `postgres`

You need to create the database before running the application:

```sql
CREATE DATABASE demo;
```

### H2 (In-memory)

For development and testing, you can use the H2 in-memory database. To use H2 instead of PostgreSQL, edit `application.properties`:

```properties
spring.profiles.active=h2
```

### Docker Profile

When running with Docker, the application uses the `docker` profile which is configured to connect to the PostgreSQL container:

- URL: `jdbc:postgresql://db:5432/postgres`
- Username: `postgres`
- Password: `postgres`

## Authentication API

The application provides REST endpoints for authentication:

### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

**Response:**
```json
{
  "token": "TOKEN_admin_1234567890",
  "username": "admin", 
  "message": "Login successful",
  "success": true
}
```

### Logout
```bash
POST /api/auth/logout
Authorization: Bearer TOKEN_admin_1234567890
```

**Response:**
```json
{
  "token": null,
  "username": "admin",
  "message": "Logout successful", 
  "success": true
}
```

## Email Configuration

The application supports multiple email providers for sending verification and password reset emails:

### Email Providers

1. **Mock Service (Default)**
   - Logs emails to console instead of sending them
   - Perfect for development and testing
   - No configuration required

2. **Amazon SES**
   - Production-ready email service
   - Requires AWS account and SES setup
   - See [SES-SETUP.md](SES-SETUP.md) for complete configuration guide

3. **Traditional SMTP**
   - Works with any SMTP server (Gmail, SendGrid, etc.)
   - Requires SMTP server credentials

### Quick Configuration

#### Using Mock Service (Default)
```bash
# No configuration needed - emails logged to console
mvn spring-boot:run
```

#### Using Amazon SES
```bash
# Set environment variables
export AWS_SES_ACCESS_KEY=your_access_key
export AWS_SES_SECRET_KEY=your_secret_key
export AWS_SES_REGION=us-east-1
export APP_FROM_EMAIL=noreply@yourdomain.com

# Run with SES profile
SPRING_PROFILES_ACTIVE=ses mvn spring-boot:run
```

#### Using SMTP (Gmail example)
```properties
app.email.provider=smtp
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

## API Documentation

The API documentation is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Database Migrations

Database migrations are handled by Liquibase. Migration scripts are located in:

```
src/main/resources/db/changelog/
```

The application includes migrations for:
1. **Person table**: Basic person information
2. **User table**: Authentication and user management 
3. **Admin user**: Pre-configured admin account with BCrypt-hashed password

## Project Structure

### Backend (Spring Boot)
- `domain`: Entity classes and repositories
- `domain.dto`: Data Transfer Objects for API communication
- `domain.mapper`: MapStruct mappers for entity-DTO conversion
- `service`: Service interfaces and implementations
- `controller`: REST controllers (including AuthController)
- `config`: Configuration classes (SecurityConfig, etc.)
- `exception`: Exception handling

### Frontend (Angular)
```
frontend/auth-app/
├── src/app/
│   ├── components/
│   │   ├── login/          # Login page component
│   │   └── dashboard/      # Dashboard page component
│   ├── services/
│   │   └── auth.service.ts # Authentication service
│   └── app-routing.module.ts # Route configuration
```

## Technologies Used

### Backend
- Spring Boot 3.3.1
- Spring Security (Authentication & Authorization)
- Spring Data JPA
- Liquibase (Database migrations)
- PostgreSQL / H2
- MapStruct (Entity-DTO mapping)
- OpenAPI/Swagger (API documentation)
- BCrypt (Password hashing)
- Lombok
- Maven

### Frontend  
- Angular 20
- TypeScript
- RxJS
- Angular Router
- HttpClient
- CSS3

### Infrastructure
- Docker & Docker Compose
- Nginx (Frontend serving)
- Multi-stage Docker builds

## Quick Start

1. **Clone the repository**
2. **Start with Docker Compose**:
   ```bash
   docker-compose up --build
   ```
3. **Access the application**: http://localhost:4200
4. **Login**: admin / admin
5. **Explore**: Navigate through login → dashboard → logout

## Troubleshooting

- If login fails, ensure the database has the admin user with correct BCrypt hash
- If frontend shows nginx default page, check the Angular build process
- For development, ensure Node.js 20+ is installed for Angular
- Check Docker logs: `docker-compose logs [service-name]` 