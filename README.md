# Person and User Management System

This Spring Boot application provides a RESTful API for managing persons and users.

## Features

- CRUD operations for Person and User entities
- Entity-DTO conversion using MapStruct
- Database migrations with Liquibase
- Support for PostgreSQL and H2 databases
- API documentation with OpenAPI/Swagger
- Docker and Docker Compose support

## Requirements

- Java 17 or higher
- Maven 3.8 or higher
- PostgreSQL (optional, can use embedded H2 instead)
- Docker and Docker Compose (optional, for containerized deployment)

## Running the Application

### Using Docker (Recommended)

#### With Docker Compose

1. Build and start all services:
   ```bash
   docker-compose up
   ```

2. Access the application at http://localhost:8080

3. To stop all services:
   ```bash
   docker-compose down
   ```

#### Using Docker Directly

1. Build the Docker image:
   ```bash
   ./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=demo:latest
   ```

2. Run the container:
   ```bash
   docker run -p 8080:8080 demo:latest
   ```

### Without Docker

1. Clone the repository
2. Navigate to the project directory
3. Run the application:

```bash
mvn spring-boot:run
```

To run with a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

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

## API Documentation

The API documentation is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Database Migrations

Database migrations are handled by Liquibase. Migration scripts are located in:

```
src/main/resources/db/changelog/
```

## Project Structure

- `domain`: Entity classes and repositories
- `domain.dto`: Data Transfer Objects
- `domain.mapper`: MapStruct mappers
- `service`: Service interfaces and implementations
- `controller`: REST controllers
- `config`: Configuration classes
- `exception`: Exception handling

## Technologies Used

- Spring Boot 3.3.1
- Spring Data JPA
- Liquibase
- PostgreSQL / H2
- MapStruct
- OpenAPI/Swagger
- Lombok
- Docker
- Docker Compose 