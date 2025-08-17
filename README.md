# Authentication & User Management System

This full-stack application provides authentication and user management functionality with a Spring Boot REST API backend and Angular frontend.

## Features

- **Authentication System**: Login/logout with JWT-like token-based authentication
- **Angular Frontend**: Modern web interface with login and dashboard pages
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