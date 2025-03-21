# Use the official Gradle image to create a build artifact
FROM gradle:8.5-jdk21 AS build

# Set the working directory
WORKDIR /home/gradle/project

# Copy the project into the Docker image
COPY . .

# Make sure the Gradle Wrapper script is executable
RUN chmod +x ./gradlew

# Build the project and run tests, disable file system watching
RUN ./gradlew :spring-boot-app:build

# Use OpenJDK for base image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /home/gradle/project/spring-boot-app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
