# ─── Stage 1: Build frontend ───────────────────────────────────────────────────
FROM node:22-alpine AS frontend-builder
WORKDIR /frontend

# Usar --mount=type=cache para cache de npm (Docker BuildKit)
RUN --mount=type=cache,target=/root/.npm npm config set cache /root/.npm
COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ─── Stage 2: Build backend (Maven) ────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /build

# Pre-descargar dependencias con cache BuildKit (se reutiliza entre builds)
COPY backend/pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline -DskipFrontend=true -q

# Copiar fuentes y frontend ya compilado
COPY backend/src ./src
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static

# Compilar el JAR usando el cache de dependencias
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -DskipFrontend=true package -q

# ─── Stage 3: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar curl para health checks
RUN apk add --no-cache curl

COPY --from=backend-builder /build/target/reviews-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health || exit 1

# Entrypoint con opciones de JVM optimizadas para contenedores
ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+ParallelRefProcEnabled", \
    "-Djava.awt.headless=true", \
    "-Dfile.encoding=UTF-8", \
    "-jar", "app.jar"]

