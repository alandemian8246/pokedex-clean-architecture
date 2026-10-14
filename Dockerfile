# syntax=docker/dockerfile:1
# Single image: the React bundle is served by the Spring Boot application on port 8080.

# 1) Build the frontend
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# 2) Build the backend with the frontend bundle as static resources
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/dist ./src/main/resources/static
# tests run in the regular build (./mvnw verify); skipped here to keep image builds fast
RUN mvn -B -q package -DskipTests

# 3) Minimal runtime image, non-root
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S pokedex && adduser -S pokedex -G pokedex
COPY --from=backend /app/target/pokedex-api-*.jar app.jar
USER pokedex
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
