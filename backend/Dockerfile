# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src ./src
ENV MAVEN_OPTS="-Xmx512m"
RUN ./mvnw -B -ntp clean package -DskipTests \
    && mv target/cidade-em-foco-*.jar target/app.jar

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system --gid 1000 appgroup \
    && useradd --system --uid 1000 --gid appgroup --home /app appuser \
    && mkdir -p /app/uploads/occurrences \
    && chown -R appuser:appgroup /app

COPY --from=build --chown=appuser:appgroup /build/target/app.jar /app/app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
