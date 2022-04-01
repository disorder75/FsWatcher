FROM openjdk:17-alpine

RUN addgroup --system spring 
RUN adduser --system spring --ingroup spring

RUN apk update && apk add sshfs;
RUN mkdir /sftpusers
#RUN sshfs root@10.80.2.133:/sftpusers /sftpusers -o nonempty -o allow_other

USER spring

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
