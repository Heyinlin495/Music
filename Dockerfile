# ============================================================
# Multi-stage Dockerfile for Music Application
# Targets: small image, fast build, secure runtime
# ============================================================

# --- Stage 1: Build backend (Maven + Spring Boot) ---
FROM maven:3.9-eclipse-temurin-21 AS build-backend
WORKDIR /build

# Copy only dependency descriptors first → cache layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B \
    && java -Djarmode=layertools -jar target/*.jar extract --destination /build/extracted

# --- Stage 2: Build frontend & admin (Node + Vite) ---
FROM node:20-alpine AS build-frontend
WORKDIR /build

# Frontend
COPY frontend/package.json frontend/package-lock.json ./frontend/
RUN cd frontend && npm ci
COPY frontend ./frontend
RUN cd frontend && npm run build

# Admin
COPY admin/package.json admin/package-lock.json ./admin/
RUN cd admin && npm ci
COPY admin ./admin
RUN cd admin && npm run build

# --- Stage 3: Production runtime ---
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Heyinlin495"
LABEL description="Music Application - Spring Boot + React"

# Install tini (proper PID 1 signal handling) and wget (healthcheck)
RUN apk add --no-cache --no-install-recommends tini wget \
    && rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Copy Spring Boot layered JAR (each layer cached independently)
# Order by change frequency: dependencies (rare) → loader → snapshots → application (frequent)
COPY --from=build-backend --chown=app:app /build/extracted/dependencies/ ./
COPY --from=build-backend --chown=app:app /build/extracted/spring-boot-loader/ ./
COPY --from=build-backend --chown=app:app /build/extracted/snapshot-dependencies/ ./
COPY --from=build-backend --chown=app:app /build/extracted/application/ ./

# Copy frontend static files (served by Spring Boot or reverse proxy)
COPY --from=build-frontend --chown=app:app /build/frontend/dist ./static/frontend
COPY --from=build-frontend --chown=app:app /build/admin/dist     ./static/admin

# Create upload/log directories
RUN mkdir -p uploads/music uploads/covers uploads/avatars logs

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["/sbin/tini", "--", "java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-cp", ".", \
  "org.springframework.boot.loader.launch.JarLauncher"]
