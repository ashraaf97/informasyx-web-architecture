# Person and User Management System

This Spring Boot application provides a RESTful API for managing persons and users.

## Features

- CRUD operations for Person and User entities
- Entity-DTO conversion using MapStruct
- Database migrations with Liquibase
- Support for PostgreSQL and H2 databases
- API documentation with OpenAPI/Swagger

## Requirements

- Java 17 or higher
- Maven 3.8 or higher
- PostgreSQL (optional, can use embedded H2 instead)

## Database Configuration

The application supports two database configurations:

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

With H2, you don't need to set up a database server as it will create an in-memory database.

## Running the Application

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