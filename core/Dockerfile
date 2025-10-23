FROM openjdk:21-jdk-slim
WORKDIR /app
ARG JAR_FILE=target/core-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8081