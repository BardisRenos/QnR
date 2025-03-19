FROM openjdk:17
LABEL maintainer="Renos Bardis"
EXPOSE 9088
ADD target/spring-boot-docker.jar spring-boot-docker.jar
ENTRYPOINT ["java", "-jar", "/spring-boot-docker.jar"]