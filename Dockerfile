
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY build/libs/*.jar app.jar
COPY redis-truststore.jks /app/redis-truststore.jks
COPY truststore.jks /app/truststore.jks
COPY aws-elasticache-ca.pem /app/aws-elasticache-ca.pem
COPY AmazonRootCA1.pem /app/AmazonRootCA1.pem


# (시스템 경로에 있는 건 읽기 전용일 수 있어서 복사해서 씁니다)
RUN cp $JAVA_HOME/lib/security/cacerts /app/cacerts


RUN keytool -importcert -alias aws-redis -file /app/aws-elasticache-ca.pem -keystore /app/cacerts -storepass changeit -noprompt


ENV JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStore=/app/cacerts -Djavax.net.ssl.trustStorePassword=changeit -Djava.net.preferIPv4Stack=true"

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
