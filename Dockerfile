# Multi-stage build for JVM mode
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Runtime stage for JVM mode
FROM eclipse-temurin:17-jre-alpine AS jvm

WORKDIR /app
COPY --from=builder /app/target/quarkus-app/lib/ /app/lib/
COPY --from=builder /app/target/quarkus-app/*.jar /app/
COPY --from=builder /app/target/quarkus-app/app/ /app/app/
COPY --from=builder /app/target/quarkus-app/quarkus/ /app/quarkus/

EXPOSE 8080

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV MODEL_PATH=/app/models

RUN mkdir -p /app/models

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/quarkus-run.jar"]

# Native image stage
FROM quay.io/quarkus/quarkus-micro-image:2.0 AS native

WORKDIR /app

COPY target/*-runner /app/application
COPY models /app/models

RUN chmod 775 /app/application

EXPOSE 8080

ENV MODEL_PATH=/app/models

ENTRYPOINT ["/app/application", "-Dquarkus.http.host=0.0.0.0"]
