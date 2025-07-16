# Job Journaler Frontend

This is the Angular frontend for the Job Journaler application. It provides a modern, responsive user interface for managing jobs/projects, creating journal templates, and maintaining weekly journal entries to document your professional journey.

## Features

- **Job Management**: Create, view, update, and complete jobs or projects
- **Journal Templates**: Create and manage reusable templates for consistent documentation
- **Weekly Journals**: Document progress through structured, template-based journal entries
- **Material Design**: Modern UI components and responsive layout
- **Reactive Forms**: Dynamic form handling for templates and journal entries
- **OAuth Authentication**: Secure login via OAuth providers (e.g., Gmail)
- **Modular Architecture**: Well-organized components and services

## Recent Updates

### July 2025 Updates

- **Improved Template Creator**: Enhanced to support both creation and updating of templates
- **Journal Content Synchronization**: Fixed issues with journal content updating
- **SCSS Refactoring**: Modularized styles with variables, common styles, and better organization
- **Dynamic Week Navigation**: Improved week selection interface for journals
- **Form Validation**: Enhanced validation for journal entries
- **Purpose Refinement**: Application now focused on professional journaling rather than being a learning tool
- **Unit Test Coverage**: Expanded test coverage for services and components

## Project Structure

```
frontend/
├── src/                         # Source code
│   ├── app/                     # Angular components and services
│   │   ├── styles/              # Global SCSS styles and variables
│   │   ├── job/                 # Job-related components
│   │   │   ├── job-journals/    # Weekly journal entries 
│   │   │   └── template-creator/# Journal template management
│   │   ├── service/             # API services
│   │   └── shared/              # Shared components and utilities
│   ├── assets/                  # Static assets
│   └── environments/            # Environment configuration
├── server/                      # Development server and mock data
└── dist/                        # Build output
```

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

For a more production-like environment, use the Docker setup from the main project:

```bash
cd ..  # Return to project root
docker-compose up
```

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Style Guide

### SCSS Organization

- Global variables in `src/app/styles/_variables.scss`
- Common styles in `src/app/styles/_common.scss`
- Component-specific styles in their respective `.scss` files

### Component Structure

- Feature-based organization (jobs, templates, journals)
- Shared components for reusable UI elements
- Service layer for API communication

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
