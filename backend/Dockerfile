# --- Etapa 1: compilar con Maven + JDK 21 ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# --- Etapa 2: solo el JRE 21 y la app ---
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 1001 sigir && mkdir -p /app/logs && chown sigir /app/logs
COPY --from=build --chown=sigir /app/target/quarkus-app/ ./
USER sigir
EXPOSE 8080
CMD ["java", "-jar", "quarkus-run.jar"]
