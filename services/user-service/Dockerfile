# Multi-stage build — compila do mono-repo e gera imagem mínima.
# Como construir (a partir da RAIZ do projeto):
#   docker build -f services/user-service/Dockerfile -t rabbitride-user-service .

# -------- Stage 1: build --------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

# Copia o necessário pra resolver dependências (camadas cacheáveis)
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY commons commons
COPY services services

RUN chmod +x mvnw && \
    ./mvnw -B -ntp -pl services/user-service -am -DskipTests package

# -------- Stage 2: runtime --------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/services/user-service/target/user-service-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
