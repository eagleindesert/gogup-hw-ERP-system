# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8083
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
