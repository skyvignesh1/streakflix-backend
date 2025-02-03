# Use Amazon Corretto as the base image
FROM amazoncorretto:17

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR file to the container
COPY target/streakflix-backend-1.0.0-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]