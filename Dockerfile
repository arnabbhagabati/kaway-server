FROM adoptopenjdk:11-jre-hotspot

chmod +x mvnw

COPY target/helloworld.jar /helloworld.jar

ENTRYPOINT java -jar helloworld.jar
