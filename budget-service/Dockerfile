FROM openjdk:21
WORKDIR /app
COPY target/budget-service-1.0-SNAPSHOT.jar app.jar
RUN chmod +r app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8081