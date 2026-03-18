# ═══════════════════════════════════════════════════════════════════════════
#  Dockerfile — AuthenticateSystem
#  Multi-stage build:
#    Stage 1 (builder) — compile + package with Maven
#    Stage 2 (runtime) — minimal JRE image, no build tools
# ═══════════════════════════════════════════════════════════════════════════

# ── Stage 1: builder ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom first — Docker layer cache:
# dependencies are only re-downloaded when pom.xml changes
COPY .mvn/           .mvn/
COPY mvnw            mvnw
COPY pom.xml         pom.xml

RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source and build
COPY src/ src/

RUN ./mvnw package -DskipTests -q

# ── Stage 2: runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user for security — never run as root in production
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the fat JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Give ownership to non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Expose application port
EXPOSE 8080

# JVM tuning:
#   -XX:+UseContainerSupport      — respect Docker CPU/memory limits
#   -XX:MaxRAMPercentage=75.0     — use 75% of container RAM for heap
#   -Djava.security.egd=...       — faster startup (avoid /dev/random block)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]