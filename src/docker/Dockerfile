FROM docker.io/eclipse-temurin:17
COPY build/libs/mqtt-to-pg-0.0.2-SNAPSHOT.jar app.jar
CMD ["java", "-Duser.timezone=CET", "-jar", "/app.jar"]