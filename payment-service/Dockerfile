FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/payment-service-1.0.jar PaymentService.jar
ENTRYPOINT ["java", "-jar", "PaymentService.jar"]