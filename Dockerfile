# 📦 Dockerfile – dla TES microservice (Spring Boot z Kafka i SQL Server)

# Etap 1: Build
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etap 2: Runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Wystaw port TES (można zmienić w application.yml)
EXPOSE 8081

# Uruchom aplikację Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
