# Use the official OpenJDK image
FROM openjdk:17-jdk-slim

# Set environment variables
ENV TZ=UTC \
    JAVA_OPTS=""

# Create a working directory
WORKDIR /app

# Copy the jar file into the container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose Spring Boot's default port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]