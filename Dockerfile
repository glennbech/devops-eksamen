FROM maven:3.6-jdk-11 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package

FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=builder /app/target/*.jar /app/devops-eksamen-pgr301-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/app/devops-eksamen-pgr301-1.0-SNAPSHOT.jar"]