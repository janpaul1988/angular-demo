# Angular Demo Project

Welcome to the **Angular Demo Project**! This project serves as a reference implementation, showcasing the integration of modern
technologies to build a full-stack application. It is designed as a learning tool for developers to explore and
understand the usage of these technologies.

## Overview

This project is built with the following technologies:

### Backend

- **Kotlin**: A modern, concise, and expressive programming language that runs on the JVM and is fully interoperable with Java.
- **Spring Boot**: A powerful framework for building Java-based microservices and RESTful APIs.
- **R2DBC**: Reactive Relational Database Connectivity for non-blocking database access.
- **MariaDB**: A fast, open-source relational database for storing application data.
- **Spring WebFlux**: A reactive programming framework for building non-blocking REST APIs.
- **SpringDoc OpenAPI**: Automatically generates API documentation and provides a Swagger UI for testing endpoints.
- **Testcontainers**: Used for spinning up lightweight, disposable containers for integration testing, ensuring consistent and isolated test environments.

---

### Frontend

- **Angular**: A modern, TypeScript-based framework for building dynamic and responsive web applications.
- **Angular Material**: A UI component library for building beautiful, consistent, and accessible user interfaces.

### DevOps

- **Docker**: Containerization technology to package and run the application in isolated environments.
- **Docker Compose**: A tool for defining and running multi-container Docker applications.

---

## Purpose

This project is meant as:

- A **reference** for using the mentioned technologies together in a full-stack application.
- A **showcase** of how to integrate these technologies effectively.
- A **learning tool** for developers to get hands-on experience with these technologies.

---

## Features

- **Reactive Backend**: Built with Kotlin, Spring Boot, WebFlux, and R2DBC for non-blocking, high-performance APIs.
- **Modern Frontend**: Developed with Angular and Angular Material for a responsive and user-friendly interface.
- **Database Integration**: Uses MariaDB for relational data storage with schema management.
- **API Documentation**: Automatically generated OpenAPI documentation with Swagger UI.
- **Containerized Deployment**: Easily deployable using Docker and Docker Compose.
- **Integration Testing**: Comprehensive integration tests using **Spring Boot Test**, **WebTestClient**, and **Testcontainers** to ensure application reliability in real-world scenarios.

---

## Getting Started

### Prerequisites

Make sure you have the following installed on your system:

- **Docker**: [Install Docker](https://docs.docker.com/get-docker/)
- **Docker Compose**: [Install Docker Compose](https://docs.docker.com/compose/install/)

### Running the Application

The entire application can be started to play around with by following these steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/janpaul1988/angular-demo.git
   cd angular-demo
   ```

2. Start Docker and run the following command:
   ```bash
   docker-compose up
   ```

3. Access the application:
    - **Frontend**: `http://localhost:80`

---

## Project Structure

```
demo-project/
├── frontend/                # Angular frontend application
├── spring-boot-app/         # Spring Boot backend application
├── docker-compose.yml       # Docker Compose configuration
└── README.md                # Project documentation
```

---

## Learning Objectives

By exploring this project, you will learn:

- How to build a **reactive backend** with Spring Boot, WebFlux, and R2DBC.
- How to create a **modern frontend** with Angular and Angular Material.
- How to integrate a **relational database** (MariaDB) with a reactive backend.
- How to use **Docker** and **Docker Compose** for containerized deployment.
- How to generate and use **API documentation** with SpringDoc OpenAPI.

---

## Forking

You are welcome to fork this project to experiment with it yourself!

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

Special thanks to the open-source community for providing the tools and frameworks used in this project.
