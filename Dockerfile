# --- Build Stage ---
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY . .
RUN gradle clean build -x test


# --- Run Stage ---
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]