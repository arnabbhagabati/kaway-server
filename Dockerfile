FROM adoptopenjdk:11-jre-hotspot

chmod +x mvnw &&

COPY target/kaway-server.jar /kaway-server.jar

ENTRYPOINT java -jar kaway-server.jar
