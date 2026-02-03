# Runtime only
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY build/libs/*.jar app.jar
COPY redis-truststore.jks /app/redis-truststore.jks

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
