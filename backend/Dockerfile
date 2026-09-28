# Build context is the repo root (see docker-compose.yml).
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY data data
COPY backend backend
RUN cd backend && chmod +x mvnw && ./mvnw package -DskipTests

FROM eclipse-temurin:25-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /workspace/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
