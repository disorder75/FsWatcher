FROM openjdk:17-jdk-slim-buster

RUN addgroup --system spring 
RUN adduser --system spring --ingroup spring

USER spring

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
