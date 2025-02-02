FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/concert-service-1.0.jar ConcertService.jar
ENTRYPOINT ["java", "-jar", "ConcertService.jar"]