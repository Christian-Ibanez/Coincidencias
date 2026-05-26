# 1. Usamos una imagen de Java 17 (que es la que vimos que usas en tus logs)
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]