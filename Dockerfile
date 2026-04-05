## Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

## Stage 2: Run
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

ENV LANGUAGE='en_US:en'

COPY --chown=185 --from=build /build/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build /build/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build /build/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

# Render injects $PORT at runtime; fall back to 8080 locally
ENTRYPOINT ["sh", "-c", "exec java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT:-8080} -Djava.util.logging.manager=org.jboss.logmanager.LogManager -jar /deployments/quarkus-run.jar"]