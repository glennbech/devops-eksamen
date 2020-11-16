#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker build . --tag devops-eksamen --build-arg JAR_FILE=./target/devops-eksamen-pgr301-1.0-SNAPSHOT.jar
docker tag devops-eksamen opkris/devops-eksamen
docker push opkris/devops-eksamen
