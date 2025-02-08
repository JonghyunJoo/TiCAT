FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/payment-service.jar PaymentService.jar
ENTRYPOINT ["java", "-jar", "PaymentService.jar"]