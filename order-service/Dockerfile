# syntax=docker/dockerfile:1

# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY commons commons
COPY customer-service/pom.xml customer-service/pom.xml
COPY order-service order-service
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl order-service -am package -DskipTests

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre
RUN groupadd --system app && useradd --system --gid app --uid 1001 app
WORKDIR /app
COPY --from=build /build/order-service/target/order-service-1.0.0.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
