# 첫 번째 단계: 애플리케이션 빌드
FROM openjdk:21-jdk AS builder
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar
# 두 번째 단계: 최종 실행 이미지 생성
FROM openjdk:21-ea-slim

# 첫 번째 단계에서 생성된 JAR 파일을 최종 이미지로 복사
COPY --from=builder build/libs/*.jar user-app.jar
VOLUME /tmp
EXPOSE 8080
# 애플리케이션 실행 명령 설정
ENTRYPOINT ["java", "-jar", "/user-app.jar"]