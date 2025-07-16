# Job Journaler

Welcome to the **Job Journaler**! This application allows users to track and journal their professional experiences
throughout jobs and projects. It provides a structured way to document progress, challenges, and achievements on a
weekly basis.

## Overview

Job Journaler is a full-stack application that helps professionals document their journey through projects and jobs.
Whether you're tracking a long-term employment position or a short-term project, this tool helps you maintain structured
records of your progress, challenges, and achievements.

## Technology Stack

Job Journaler is built with the following technologies:

### Backend (Spring Boot)

- **Kotlin**: Modern programming language for the backend
- **Spring Boot**: Framework for building the RESTful API
- **R2DBC**: Reactive database connectivity
- **MariaDB**: Database for storing job and journal data
- **Spring WebFlux**: Reactive programming framework
- **SpringDoc OpenAPI**: API documentation

### BFF

BFF (Backend For Frontend) & OAuth2 Authentication
This project includes a BFF (Backend For Frontend) service that acts as a secure gateway between the Angular frontend
and the backend API. The BFF handles OAuth2 authentication (e.g., with Google/Gmail) and ensures that only authenticated
users can access protected resources.

#### How it works

* The Angular frontend sends API requests to the BFF, not directly to the backend.
* The BFF checks for a valid OAuth2 token on each request.
* If no valid token is present, the BFF redirects the user to the OAuth2 provider’s login page.
* After successful authentication, the BFF receives the token, manages the session, and proxies requests to the backend
  with the appropriate credentials.
* The backend uses the token data (e.g., email) to identify or provision users as needed.

#### Configuring OAuth2

To use OAuth2 authentication (e.g., with Google), you must register your application with your OAuth provider and obtain
a client ID and client secret.

1. Register your app with your OAuth provider (e.g., Google Cloud Console).
2. Create a .env file in the root of the repository (next to docker-compose.yml) with the following content:

Replace the values with your actual credentials.
You can generate a random cookie secret with:
`openssl rand -base64 32`
Do NOT commit your .env file to version control.
Make sure .env is listed in your .gitignore.

The Docker Compose setup will automatically use these values to configure the BFF for OAuth2 authentication.

### Frontend (Angular)

- **Angular**: Framework for building the web application
- **Angular Material**: UI component library for a modern interface
- **Reactive Forms**: Dynamic form handling for templates and journals

### Deployment

- **Docker**: Containerization for consistent deployment
- **Docker Compose**: Multi-container orchestration

---

## Features

- **Job Management**: Create, track, update, and complete jobs or projects
- **Journal Templates**: Create reusable templates for consistent documentation
- **Weekly Journals**: Document progress through structured journal entries
- **User Authentication**: Secure access via OAuth providers
- **Responsive Design**: Works on desktop and mobile devices

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
   git clone https://github.com/janpaul1988/job-journaler.git
   cd job-journaler
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
job-journaler/
├── frontend/                # Angular frontend application
├── spring-boot-app/         # Spring Boot backend application
├── docker-compose.yml       # Docker Compose configuration
└── README.md                # Project documentation
```

---

## Use Cases

Job Journaler is designed to help you:

- **Track Multiple Jobs**: Maintain records of different positions or projects simultaneously
- **Create Consistent Reports**: Use templates to ensure you're capturing the same key information weekly
- **Review Progress**: Look back at weekly journals to assess growth and accomplishments
- **Document Challenges**: Record obstacles encountered and solutions implemented
- **Build Your Portfolio**: Compile a detailed history of your professional experiences

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

Special thanks to the open-source community for providing the tools and frameworks used in this project.
