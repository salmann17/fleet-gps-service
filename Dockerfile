# ─────────────────────────────────────────
# Stage 1 – Build
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /build

COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -B -DskipTests

# ─────────────────────────────────────────
# Stage 2 – Runtime
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jre

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
