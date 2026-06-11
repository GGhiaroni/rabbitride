# -------- Stage 1: build --------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY commons commons
COPY services services

RUN chmod +x mvnw && \
    ./mvnw -B -ntp -pl services/analysis-service -am -DskipTests package

# -------- Stage 2: runtime --------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/services/analysis-service/target/analysis-service-*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
