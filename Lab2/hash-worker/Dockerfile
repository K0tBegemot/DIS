FROM gradle:8.5.0-jdk21-alpine AS builder
COPY src /usr/src/app/src
COPY build.gradle.kts /usr/src/app
COPY settings.gradle.kts /usr/src/app
ARG K0TBEGEM0T_TOKEN
ARG K0TBEGEM0T_USERNAME
RUN --mount=type=cache,target=/home/gradle/.gradle gradle -p /usr/src/app -i bootJar
WORKDIR /extracted
RUN java -Djarmode=layertools -jar /usr/src/app/build/libs/*.jar extract
FROM eclipse-temurin:21-jre-alpine
WORKDIR application
COPY --from=builder /extracted/dependencies ./
COPY --from=builder /extracted/spring-boot-loader ./
COPY --from=builder /extracted/snapshot-dependencies ./
COPY --from=builder /extracted/application/ ./
EXPOSE 8080
ARG MAIN_BACKEND_PORT
#RUN apk --no-cache add curl
HEALTHCHECK --interval=5s --timeout=3s --retries=3 --start-period=30s \
CMD curl http://localhost:$MAIN_BACKEND_PORT/actuator/health
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]