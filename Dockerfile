# Runtime only
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY redis-truststore.jks /app/redis-truststore.jks
COPY truststore.jks /app/truststore.jks
COPY aws-elasticache-ca.pem /app/aws-elasticache-ca.pem
COPY AmazonRootCA1.pem /app/AmazonRootCA1.pem

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
