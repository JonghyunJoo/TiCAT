FROM openjdk:21
VOLUME /tmp
COPY apiEncryptionKey.jks apiEncryptionKey.jks
COPY build/libs/config-service.jar ConfigServer.jar
ENTRYPOINT ["java","-jar","ConfigServer.jar"]