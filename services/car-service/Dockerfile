# -------- Stage 1: build --------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY commons commons
COPY services services

RUN chmod +x mvnw && \
    ./mvnw -B -ntp -pl services/car-service -am -DskipTests package

# -------- Stage 2: runtime --------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/services/car-service/target/car-service-*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
