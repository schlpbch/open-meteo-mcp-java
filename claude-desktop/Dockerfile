# Multi-stage build for Open-Meteo MCP Java Server
FROM maven:3.9-openjdk-21-slim AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim AS runtime

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built JAR
COPY --from=build /workspace/target/*.jar app.jar

# Create non-root user
RUN groupadd -r mcp && useradd -r -g mcp mcp
RUN chown -R mcp:mcp /app
USER mcp

# Expose ports
EXPOSE 8080 8081 5005

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $JAVA_TOOL_OPTIONS -jar app.jar"]

# Production stage for minimal runtime
FROM gcr.io/distroless/java21-debian12 AS production

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080 8081
USER nonroot:nonroot

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]