# Stage 1: Build application and cache dependencies
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and checkstyle config, package artifact
COPY src ./src
COPY checkstyle.xml .
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal runtime environment
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Create a non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled jar using wildcard from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Grant ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]