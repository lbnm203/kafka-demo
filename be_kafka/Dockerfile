# ─────────────────────────────────────────────────────────────
# Multi-stage build để image nhỏ hơn (~200MB thay vì ~600MB)
# ─────────────────────────────────────────────────────────────

# Stage 1: Build với Gradle
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy gradle wrapper & config trước (cache layer khi code thay đổi)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Download dependencies (tách layer để cache)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet 2>/dev/null || true

# Copy source và build
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime image gọn nhẹ
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Thêm wget cho healthcheck
RUN apk add --no-cache wget

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
