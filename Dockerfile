# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Make maven script executable
RUN sed -i 's/\\r$//' mvnw && chmod +x ./mvnw
# Go offline to cache dependencies
RUN ./mvnw dependency:go-offline -B
# Copy source code and build
COPY src src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root user and setup directories with proper permissions
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/storage /app/target && \
    chown -R spring:spring /app

USER spring:spring

# Copy jar from builder
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

ENV STORAGE_LOCAL_BASE_DIR=/app/storage

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
