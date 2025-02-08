FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/reservation-service.jar ReservationService.jar
ENTRYPOINT ["java", "-jar", "ReservationService.jar"]