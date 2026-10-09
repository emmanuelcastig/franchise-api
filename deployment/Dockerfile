# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle main.gradle gradle.properties lombok.config ./
COPY gradle ./gradle
COPY domain ./domain
COPY infrastructure ./infrastructure
COPY applications ./applications
RUN --mount=type=cache,target=/root/.gradle \
    sh ./gradlew :app-service:bootJar --no-daemon --no-configuration-cache -x test

FROM eclipse-temurin:25-jdk-alpine AS layers
WORKDIR /layers
COPY --from=build /workspace/applications/app-service/build/libs/franchise-api.jar franchise-api.jar
RUN java -Djarmode=tools -jar franchise-api.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre-alpine AS runtime
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=layers --chown=app:app /layers/extracted/dependencies/ ./
COPY --from=layers --chown=app:app /layers/extracted/spring-boot-loader/ ./
COPY --from=layers --chown=app:app /layers/extracted/snapshot-dependencies/ ./
COPY --from=layers --chown=app:app /layers/extracted/application/ ./
USER app

RUN DB_INITIALIZE_SCHEMA=false java -XX:AOTCacheOutput=franchise-api.aot -Dspring.context.exit=onRefresh \
    -jar franchise-api.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError --enable-native-access=ALL-UNNAMED" \
    TZ="America/Bogota"
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-XX:AOTCache=franchise-api.aot", "-jar", "franchise-api.jar"]
