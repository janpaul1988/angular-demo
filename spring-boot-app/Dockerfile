# Use OpenJDK for base image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY spring-boot-app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
