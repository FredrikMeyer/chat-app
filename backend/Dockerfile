FROM openjdk:8u181-alpine3.8

WORKDIR /

COPY ./target/uberjar/chat-app-0.1.0-SNAPSHOT-standalone.jar chat-app.jar
EXPOSE 8888

CMD java -jar chat-app.jar
