FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/concert-service.jar ConcertService.jar
ENTRYPOINT ["java", "-jar", "ConcertService.jar"]