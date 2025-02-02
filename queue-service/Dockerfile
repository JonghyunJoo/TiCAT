FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/queue-service-1.0.jar QueueService.jar
ENTRYPOINT ["java", "-jar", "QueueService.jar"]