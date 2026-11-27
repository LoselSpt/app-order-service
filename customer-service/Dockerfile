# syntax=docker/dockerfile:1

# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY commons commons
COPY order-service/pom.xml order-service/pom.xml
COPY customer-service customer-service
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl customer-service -am package -DskipTests

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre
RUN groupadd --system app && useradd --system --gid app --uid 1001 app
WORKDIR /app
COPY --from=build /build/customer-service/target/customer-service-1.0.0.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
