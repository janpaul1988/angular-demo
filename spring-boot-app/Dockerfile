# Use the official Gradle image to create a build artifact
FROM gradle:7.2.0-jdk17 AS build

# Set the working directory
WORKDIR /home/gradle/project

# Copy the project into the Docker image
COPY . .

# Make sure the Gradle Wrapper script is executable
RUN chmod +x ./gradlew

# Build the project and skip tests, disable file system watching
RUN ./gradlew build -x test

# Use OpenJDK for base image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]