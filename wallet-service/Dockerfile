FROM openjdk:21-ea-slim
VOLUME /tmp
COPY build/libs/wallet-service-1.0.jar WalletService.jar
ENTRYPOINT ["java", "-jar", "WalletService.jar"]