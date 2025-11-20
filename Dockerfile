FROM eclipse-temurin:21-jdk-alpine

COPY /target/order_service-0.0.1-SNAPSHOT.jar /order-service/order-service.jar

WORKDIR /order-service

EXPOSE 8083

ENTRYPOINT [ "java","-jar","order-service.jar", "--spring.profiles.active=docker" ]