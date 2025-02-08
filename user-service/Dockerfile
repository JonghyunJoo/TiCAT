FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/user-service.jar UserService.jar
ENTRYPOINT ["java", "-jar", "UserService.jar"]