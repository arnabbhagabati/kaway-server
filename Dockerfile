FROM adoptopenjdk:11-jre-hotspot

COPY target/kaway-server.jar /kaway-server.jar

ENTRYPOINT java -jar kaway-server.jar
