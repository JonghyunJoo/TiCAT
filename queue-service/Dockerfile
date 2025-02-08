FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/queue-service.jar QueueService.jar
ENTRYPOINT ["java", "-jar", "QueueService.jar"]