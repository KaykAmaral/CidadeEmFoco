# syntax=docker/dockerfile:1

########################
# Stage 1: build (Maven via mvnw, JDK 21)
########################
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

# Copia primeiro o wrapper e o pom.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Agora copia o código-fonte e empacota com limite de memória para Maven.
COPY src ./src
ENV MAVEN_OPTS="-Xmx512m"
RUN ./mvnw -B -ntp clean package -DskipTests \
    && mv target/cidade-em-foco-*.jar target/app.jar

########################
# Stage 2: runtime (somente JRE, imagem enxuta)
########################
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Usuário non-root dedicado
RUN groupadd --system --gid 1000 appgroup \
    && useradd --system --uid 1000 --gid appgroup --home /app appuser \
    && mkdir -p /app/uploads/occurrences \
    && chown -R appuser:appgroup /app

COPY --from=build --chown=appuser:appgroup /build/target/app.jar /app/app.jar

USER appuser

# Porta padrão documentada; o mapeamento real de porta é feito no docker-compose.yml
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
