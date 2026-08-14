FROM node:20-alpine AS frontend

WORKDIR /fe

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend

WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src

RUN rm -rf src/main/resources/static && \
    mkdir -p src/main/resources/static

COPY --from=frontend /fe/dist/ src/main/resources/static/

# Tests need Docker (Testcontainers), so run them with `mvn test` outside the image build.
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN adduser -D app
USER app
WORKDIR /app

COPY --from=backend \
     /app/target/distributed-rate-limiter-1.0.0.jar \
     app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
