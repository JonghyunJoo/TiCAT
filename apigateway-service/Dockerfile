FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/apigateway-service.jar ApiGatewayService.jar
ENTRYPOINT ["java", "-jar", "ApiGatewayService.jar"]