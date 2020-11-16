hello world 😁

This project cant be private in git.
Travis-ci don't like it 😢

For å lage en Docker Container av Spring Boot applikasjonen din må du lage en Dockerfile

    FROM openjdk:8-jdk-alpine
    VOLUME /tmp
    ARG JAR_FILE
    COPY ${JAR_FILE} app.jar
    ENTRYPOINT ["java","-jar","app.jar"]
    For å bruke Docker til å lage et Container Image kjører dere;

docker build . --tag pgr301 --build-arg JAR_FILE=./build/libs/<artifactname>
Artifactname er filnavnet på JAR filen. Merk at dere må bygge med Maven eller Gradle før dere kjører kommandoen. Hvis dere bygger med Maven er ikke JAR_FILE argumentet build/libs men target/xyz...

For å starte en Container, kan du kjøre

    docker run -p 8080:8080 pgr301:latest
    
Du skal nå kunne kjøre nå applikasjonen din fra nettleser.