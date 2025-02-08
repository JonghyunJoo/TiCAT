FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/service-discovery.jar ServiceDiscovery.jar
ENTRYPOINT ["java", "-jar", "ServiceDiscovery.jar"]