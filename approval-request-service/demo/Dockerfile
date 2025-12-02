# Build stage
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy proto files
COPY proto/ /app/proto/

# Copy gradle files
COPY approval-request-service/demo/build.gradle /app/approval-request-service/demo/
COPY approval-request-service/demo/settings.gradle /app/approval-request-service/demo/
COPY approval-request-service/demo/gradlew /app/approval-request-service/demo/
COPY approval-request-service/demo/gradlew.bat /app/approval-request-service/demo/
COPY approval-request-service/demo/gradle /app/approval-request-service/demo/gradle

WORKDIR /app/approval-request-service/demo

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY approval-request-service/demo/src /app/approval-request-service/demo/src

# Build the application
RUN gradle bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built jar
COPY --from=build /app/approval-request-service/demo/build/libs/*.jar app.jar

# Expose ports
EXPOSE 8082
EXPOSE 9091

# Set environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
