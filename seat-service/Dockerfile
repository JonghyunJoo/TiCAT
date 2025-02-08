FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/seat-service.jar SeatService.jar
ENTRYPOINT ["java", "-jar", "SeatService.jar"]