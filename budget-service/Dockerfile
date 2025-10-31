FROM openjdk:21-jdk AS builder

WORKDIR /app
COPY target/budget-service-1.0-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract
FROM openjdk:21-jdk
WORKDIR /app

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
EXPOSE 8081