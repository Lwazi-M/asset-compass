# Stage 1: Build the Java JAR file
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies first to cache them and speed up future builds
RUN mvn dependency:go-offline
COPY src ./src
# Compile the code and skip tests for a faster production build
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight runtime environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy ONLY the compiled JAR from Stage 1 (shrinks image from 1GB to ~150MB)
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]