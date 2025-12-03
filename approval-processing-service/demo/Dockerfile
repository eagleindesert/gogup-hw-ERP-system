# Build stage
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy proto files
COPY proto/ /app/proto/

# Copy gradle files
COPY approval-processing-service/demo/build.gradle /app/approval-processing-service/demo/
COPY approval-processing-service/demo/settings.gradle /app/approval-processing-service/demo/
COPY approval-processing-service/demo/gradlew /app/approval-processing-service/demo/
COPY approval-processing-service/demo/gradlew.bat /app/approval-processing-service/demo/
COPY approval-processing-service/demo/gradle /app/approval-processing-service/demo/gradle

WORKDIR /app/approval-processing-service/demo

# Download dependencies
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY approval-processing-service/demo/src /app/approval-processing-service/demo/src

# Build the application
RUN gradle bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built jar
COPY --from=build /app/approval-processing-service/demo/build/libs/*.jar app.jar

# Expose ports
EXPOSE 8083
EXPOSE 9090

# Set environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
