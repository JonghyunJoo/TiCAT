FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/reservation-service-1.0.jar ReservationService.jar
ENTRYPOINT ["java", "-jar", "ReservationService.jar"]