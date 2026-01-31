FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar built externally (by GitHub Actions)
COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]