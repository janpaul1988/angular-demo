# Job Journaler Backend

This is the Spring Boot backend for the Job Journaler application. It provides the RESTful API for managing
jobs/projects and journal entries.

## Features

- **Job Management API**: Endpoints for creating, retrieving, updating, and completing jobs/projects
- **Journal Templates API**: Support for creating and managing reusable journal templates
- **Weekly Journal API**: Endpoints for documenting weekly progress through structured entries
- **User Authentication**: Integration with OAuth providers via the BFF
- **Reactive Architecture**: Built with WebFlux and R2DBC for non-blocking performance

## Getting Started

### Prerequisites

Make sure you have the following installed on your system:

- **JDK 17+**: Required for running the Spring Boot application
- **Docker**: For running the containerized version or dependencies
- **MariaDB**: The database backend (can be run via Docker)

### Running Locally for Development

1. Start MariaDB:
   ```bash
   docker-compose up -d mariadb
   ```

2. Run the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```

3. Access the API at:
    - API: `http://localhost:8080`
    - API Documentation: `http://localhost:8080/swagger-ui.html`

### Configuration

The application can be configured through `application.yml`. Key configuration options include:

- Database connection settings
- Authentication settings
- Logging levels
- CORS configuration

## API Documentation

API documentation is automatically generated using SpringDoc OpenAPI and can be accessed at `/swagger-ui.html` when the
application is running.

## Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
* [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
* [R2DBC Documentation](https://r2dbc.io/)
